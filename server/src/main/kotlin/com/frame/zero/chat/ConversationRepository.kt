package com.frame.zero.chat

import com.frame.zero.common.isUniqueViolation
import com.frame.zero.common.nowTruncatedToMicros
import com.frame.zero.config.dbQuery
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.sql.SQLException
import java.util.UUID
import kotlin.time.Instant

enum class ConversationKind {
  TASK,
  DIRECT
}

data class ConversationRecord(
  val id: UUID,
  val kind: ConversationKind,
  val taskId: UUID?,
  val productionId: UUID,
  val createdAt: Instant
)

interface ConversationRepository {
  suspend fun findById(id: UUID): ConversationRecord?

  suspend fun findByTaskId(taskId: UUID): ConversationRecord?

  /**
   * Returns the task's conversation, creating it on first call. Safe under a
   * concurrent get-or-create race: a caller that loses the insert on the unique
   * `task_id` index reads back the winner instead of surfacing a 500.
   */
  suspend fun getOrCreateTaskConversation(
    taskId: UUID,
    productionId: UUID
  ): ConversationRecord

  /** Lazily records a read-state row for [userId]; a no-op if one already exists. */
  suspend fun ensureParticipant(
    conversationId: UUID,
    userId: UUID
  )
}

class ConversationRepositoryImpl : ConversationRepository {
  override suspend fun findById(id: UUID): ConversationRecord? =
    dbQuery {
      ConversationsTable
        .selectAll()
        .where { ConversationsTable.id eq id }
        .singleOrNull()
        ?.toRecord()
    }

  override suspend fun findByTaskId(taskId: UUID): ConversationRecord? =
    dbQuery {
      ConversationsTable
        .selectAll()
        .where { ConversationsTable.taskId eq taskId }
        .singleOrNull()
        ?.toRecord()
    }

  override suspend fun getOrCreateTaskConversation(
    taskId: UUID,
    productionId: UUID
  ): ConversationRecord =
    dbQuery {
      findTaskConversation(taskId) ?: run {
        val now = nowTruncatedToMicros()
        val newId = UUID.randomUUID()
        // Postgres aborts the whole transaction on a constraint violation, so the
        // recovery read below would itself fail with "current transaction is
        // aborted" without a savepoint to roll back to first.
        val savepoint = connection.setSavepoint("get_or_create_task_conversation")
        try {
          ConversationsTable.insert {
            it[id] = newId
            it[kind] = ConversationKind.TASK.name
            it[ConversationsTable.taskId] = taskId
            it[ConversationsTable.productionId] = productionId
            it[createdAt] = now
          }
          connection.releaseSavepoint(savepoint)
          ConversationRecord(newId, ConversationKind.TASK, taskId, productionId, now)
        } catch (exception: SQLException) {
          // A concurrent get-or-create won the insert race on conversations_task_unique;
          // roll back to the savepoint and return its row rather than a 500.
          if (exception.isUniqueViolation()) {
            connection.rollback(savepoint)
            findTaskConversation(taskId) ?: throw exception
          } else {
            throw exception
          }
        }
      }
    }

  override suspend fun ensureParticipant(
    conversationId: UUID,
    userId: UUID
  ) {
    dbQuery {
      ConversationParticipantsTable.insertIgnore {
        it[ConversationParticipantsTable.conversationId] = conversationId
        it[ConversationParticipantsTable.userId] = userId
        it[lastReadOrdinal] = 0
        it[joinedAt] = nowTruncatedToMicros()
      }
    }
  }

  private fun findTaskConversation(taskId: UUID): ConversationRecord? =
    ConversationsTable
      .selectAll()
      .where { ConversationsTable.taskId eq taskId }
      .singleOrNull()
      ?.toRecord()

  private fun ResultRow.toRecord(): ConversationRecord =
    ConversationRecord(
      id = this[ConversationsTable.id],
      kind = ConversationKind.valueOf(this[ConversationsTable.kind]),
      taskId = this[ConversationsTable.taskId],
      productionId = this[ConversationsTable.productionId],
      createdAt = this[ConversationsTable.createdAt]
    )
}
