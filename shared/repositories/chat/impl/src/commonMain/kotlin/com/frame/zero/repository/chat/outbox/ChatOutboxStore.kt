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
    dao.insert(
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

  /** Head of the queue for [conversationId]; null when it is drained or already has a send in flight. */
  suspend fun nextQueued(conversationId: String): PendingChatMessage? =
    dao.nextQueued(conversationId, PendingMessageStatus.Queued.name, PendingMessageStatus.Sending.name)?.toDomain()

  /**
   * Takes ownership of a queued message. False means a concurrent drain got there first, and this
   * caller must not send it — the compare-and-set, not the caller's memory, is what guarantees a
   * conversation only ever has one message in flight.
   */
  suspend fun claim(
    conversationId: String,
    clientMessageId: String
  ): Boolean =
    dao.claim(
      conversationId = conversationId,
      clientMessageId = clientMessageId,
      queued = PendingMessageStatus.Queued.name,
      sending = PendingMessageStatus.Sending.name
    ) > 0

  /**
   * Returns this conversation's in-flight rows to the queue. The drain calls it under its
   * per-conversation lock, so a `Sending` row can only be one a killed or cancelled drain stranded,
   * never a live send.
   */
  suspend fun resetInFlight(conversationId: String): Int =
    dao.resetInFlight(conversationId, PendingMessageStatus.Queued.name, PendingMessageStatus.Sending.name)

  suspend fun conversationsWithPending(): List<String> =
    dao.conversationsWithPending(PendingMessageStatus.Queued.name, PendingMessageStatus.Sending.name)

  suspend fun get(
    conversationId: String,
    clientMessageId: String
  ): PendingChatMessage? = dao.get(conversationId, clientMessageId)?.toDomain()

  /** Back to the queue without spending an attempt — for "no network", which isn't a delivery failure. */
  suspend fun resetToQueued(
    conversationId: String,
    clientMessageId: String
  ) = dao.updateStatus(conversationId, clientMessageId, PendingMessageStatus.Queued.name)

  /** Back to the queue after a server-side transient failure; the attempt is counted so the drain can give up. */
  suspend fun requeue(
    conversationId: String,
    clientMessageId: String
  ) = dao.updateStatusAndCountAttempt(conversationId, clientMessageId, PendingMessageStatus.Queued.name)

  suspend fun markFailed(
    conversationId: String,
    clientMessageId: String
  ) = dao.updateStatusAndCountAttempt(conversationId, clientMessageId, PendingMessageStatus.Failed.name)

  /** User-driven retry of a failed message; keeps the id, so the resend stays idempotent. */
  suspend fun retry(
    conversationId: String,
    clientMessageId: String
  ) = resetToQueued(conversationId, clientMessageId)

  suspend fun remove(
    conversationId: String,
    clientMessageId: String
  ) = dao.delete(conversationId, clientMessageId)
}
