package com.frame.zero.repository.chat.outbox

import com.frame.zero.database.ChatOutboxDao
import com.frame.zero.database.PendingMessageEntity
import com.frame.zero.domain.chat.PendingChatMessage
import com.frame.zero.domain.chat.PendingMessageStatus
import com.frame.zero.repository.chat.local.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

/**
 * Domain-typed view of the outbox table. Keeps the status-enum-to-column-string translation in one
 * place so neither the drain nor the repository handles raw entities.
 */
internal class ChatOutboxStore(
  private val dao: ChatOutboxDao,
  private val clock: Clock = Clock.System
) {
  fun observe(conversationId: String): Flow<List<PendingChatMessage>> =
    dao.observeByConversation(conversationId).map { rows -> rows.map { it.toDomain() } }

  suspend fun enqueue(
    conversationId: String,
    clientMessageId: String,
    body: String
  ) {
    dao.upsert(
      PendingMessageEntity(
        clientMessageId = clientMessageId,
        conversationId = conversationId,
        body = body,
        status = PendingMessageStatus.Queued.name,
        attemptCount = 0,
        createdAtEpochMs = clock.now().toEpochMilliseconds()
      )
    )
  }

  /** Head of the queue for [conversationId], or null when there is nothing left to send. */
  suspend fun nextQueued(conversationId: String): PendingChatMessage? =
    dao.nextQueued(conversationId, PendingMessageStatus.Queued.name)?.toDomain()

  suspend fun conversationsWithQueued(): List<String> =
    dao.conversationsWithQueued(PendingMessageStatus.Queued.name)

  suspend fun get(clientMessageId: String): PendingChatMessage? = dao.get(clientMessageId)?.toDomain()

  suspend fun markSending(clientMessageId: String) = dao.updateStatus(clientMessageId, PendingMessageStatus.Sending.name)

  /** Back to the queue after a transient failure; the attempt is counted for backoff/diagnostics. */
  suspend fun requeue(clientMessageId: String) =
    dao.updateStatusAndCountAttempt(clientMessageId, PendingMessageStatus.Queued.name)

  suspend fun markFailed(clientMessageId: String) =
    dao.updateStatusAndCountAttempt(clientMessageId, PendingMessageStatus.Failed.name)

  /** User-driven retry of a permanently failed message; keeps the id, so the send stays idempotent. */
  suspend fun retry(clientMessageId: String) = dao.updateStatus(clientMessageId, PendingMessageStatus.Queued.name)

  suspend fun remove(clientMessageId: String) = dao.delete(clientMessageId)
}
