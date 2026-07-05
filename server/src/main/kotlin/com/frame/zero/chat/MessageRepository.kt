package com.frame.zero.chat

import com.frame.zero.common.isUniqueViolation
import com.frame.zero.common.nowTruncatedToMicros
import com.frame.zero.config.dbQuery
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.sql.SQLException
import java.util.UUID
import kotlin.time.Instant

data class MessageRecord(
  val id: UUID,
  val conversationId: UUID,
  val ordinal: Long,
  val senderUserId: UUID,
  val body: String,
  val clientMessageId: String,
  val createdAt: Instant
)

/** [message] is the canonical row; [isNew] is false when [message] was already persisted (a replay). */
data class AppendResult(
  val message: MessageRecord,
  val isNew: Boolean
)

interface MessageRepository {
  /**
   * Inserts the next message in [conversationId] with a server-assigned [ordinal],
   * idempotent on `(conversationId, senderUserId, clientMessageId)`. A retried send
   * with the same client id in the same conversation returns the already-persisted
   * message without advancing `ordinal`, and [AppendResult.isNew] is false so callers
   * know not to re-fan-out a duplicate.
   */
  suspend fun append(
    conversationId: UUID,
    senderUserId: UUID,
    body: String,
    clientMessageId: String
  ): AppendResult

  /**
   * Newest-first page of messages, optionally older than [before] (exclusive)
   * for backfill. Returns up to [limit] rows.
   */
  suspend fun findByConversation(
    conversationId: UUID,
    before: Long?,
    limit: Int
  ): List<MessageRecord>

  /** Highest `ordinal` in [conversationId], or 0 when the conversation has no messages yet. */
  suspend fun maxOrdinal(conversationId: UUID): Long
}

class MessageRepositoryImpl : MessageRepository {
  override suspend fun append(
    conversationId: UUID,
    senderUserId: UUID,
    body: String,
    clientMessageId: String
  ): AppendResult =
    dbQuery {
      findByClientId(conversationId, senderUserId, clientMessageId)
        ?.let { return@dbQuery AppendResult(it, isNew = false) }

      // Lock the conversation row so concurrent sends serialize: two of them can't
      // read the same MAX(ordinal) and collide on messages_conversation_ordinal_unique.
      ConversationsTable
        .selectAll()
        .where { ConversationsTable.id eq conversationId }
        .forUpdate()
        .singleOrNull()

      val currentMax = MessagesTable
        .select(MessagesTable.ordinal)
        .where { MessagesTable.conversationId eq conversationId }
        .orderBy(MessagesTable.ordinal to SortOrder.DESC)
        .limit(1)
        .firstOrNull()
        ?.get(MessagesTable.ordinal)
      val nextOrdinal = (currentMax ?: 0L) + 1
      val newId = UUID.randomUUID()
      val now = nowTruncatedToMicros()

      // Postgres aborts the whole transaction on a constraint violation, so the
      // recovery read below would itself fail with "current transaction is
      // aborted" without a savepoint to roll back to first.
      val savepoint = connection.setSavepoint("append_message")
      try {
        MessagesTable.insert {
          it[id] = newId
          it[MessagesTable.conversationId] = conversationId
          it[ordinal] = nextOrdinal
          it[MessagesTable.senderUserId] = senderUserId
          it[MessagesTable.body] = body
          it[MessagesTable.clientMessageId] = clientMessageId
          it[createdAt] = now
        }
        connection.releaseSavepoint(savepoint)
      } catch (exception: SQLException) {
        // A concurrent retry with the same client id won the race and collided on
        // messages_conv_sender_client_id_unique; roll back to the savepoint and
        // return its canonical row.
        if (exception.isUniqueViolation()) {
          connection.rollback(savepoint)
          val existing = findByClientId(conversationId, senderUserId, clientMessageId) ?: throw exception
          return@dbQuery AppendResult(existing, isNew = false)
        }
        throw exception
      }

      AppendResult(
        MessageRecord(newId, conversationId, nextOrdinal, senderUserId, body, clientMessageId, now),
        isNew = true
      )
    }

  override suspend fun findByConversation(
    conversationId: UUID,
    before: Long?,
    limit: Int
  ): List<MessageRecord> =
    dbQuery {
      MessagesTable
        .selectAll()
        .where {
          var cond = MessagesTable.conversationId eq conversationId
          if (before != null) cond = cond and (MessagesTable.ordinal less before)
          cond
        }.orderBy(MessagesTable.ordinal to SortOrder.DESC)
        .limit(limit)
        .map { it.toRecord() }
    }

  override suspend fun maxOrdinal(conversationId: UUID): Long =
    dbQuery {
      MessagesTable
        .select(MessagesTable.ordinal)
        .where { MessagesTable.conversationId eq conversationId }
        .orderBy(MessagesTable.ordinal to SortOrder.DESC)
        .limit(1)
        .firstOrNull()
        ?.get(MessagesTable.ordinal) ?: 0L
    }

  // Must run inside a dbQuery transaction.
  private fun findByClientId(
    conversationId: UUID,
    senderUserId: UUID,
    clientMessageId: String
  ): MessageRecord? =
    MessagesTable
      .selectAll()
      .where {
        (MessagesTable.conversationId eq conversationId) and
          (MessagesTable.senderUserId eq senderUserId) and
          (MessagesTable.clientMessageId eq clientMessageId)
      }.singleOrNull()
      ?.toRecord()

  private fun ResultRow.toRecord(): MessageRecord =
    MessageRecord(
      id = this[MessagesTable.id],
      conversationId = this[MessagesTable.conversationId],
      ordinal = this[MessagesTable.ordinal],
      senderUserId = this[MessagesTable.senderUserId],
      body = this[MessagesTable.body],
      clientMessageId = this[MessagesTable.clientMessageId],
      createdAt = this[MessagesTable.createdAt]
    )
}
