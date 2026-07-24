package com.frame.zero.repository.chat.outbox

import com.frame.zero.core.logging.Logger
import com.frame.zero.database.ChatDao
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.chat.PendingChatMessage
import com.frame.zero.domain.toDomainError
import com.frame.zero.dto.chat.SendMessageRequest
import com.frame.zero.repository.chat.local.toEntity
import com.frame.zero.repository.chat.network.ChatApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Sends queued messages, strictly one at a time per conversation: send, await the response, only
 * then take the next. The server assigns each message's `ordinal` when it arrives, so anything
 * concurrent (or retried out of order) would publish a conversation in an order nobody typed —
 * which is why the generic `core/upload` scheduler is not reused here.
 *
 * Serialization is enforced twice over: a per-conversation [Mutex] for the in-process case, and a
 * compare-and-set claim in SQL for everything else (a WorkManager worker draining while the UI
 * sends, say). Duplicate delivery is impossible regardless — the server dedupes on
 * `clientMessageId`.
 */
internal class ChatOutbox(
  private val store: ChatOutboxStore,
  private val api: ChatApi,
  private val chatDao: ChatDao,
  private val logger: Logger
) {
  private val conversationMutexes = mutableMapOf<String, Mutex>()
  private val mutexGuard = Mutex()

  /**
   * Flushes every conversation with anything pending — including rows a killed process left in
   * flight, which [conversationsWithPending] surfaces and [drain] then recovers. Used on reconnect,
   * sign-in and app start.
   */
  suspend fun drainAll() {
    store.conversationsWithPending().forEach { drain(it) }
  }

  /**
   * Sends everything queued for [conversationId]. Returns false when it stopped with work left —
   * a transient failure the caller (WorkManager, connectivity, reconnect) should re-kick.
   */
  suspend fun drain(conversationId: String): Boolean =
    mutexFor(conversationId).withLock {
      // Recover anything a killed or cancelled drain of this conversation stranded mid-send. Safe
      // here because the lock guarantees no other drain of this conversation is running, so a
      // Sending row can only be leftover — never a live send this would trample.
      store.resetInFlight(conversationId)
      while (true) {
        val message = store.nextQueued(conversationId) ?: break
        // Claim is belt-and-suspenders under the lock; if it ever loses, back off rather than
        // skipping ahead to a younger message.
        if (!store.claim(conversationId, message.clientMessageId)) break
        if (!send(message)) return@withLock false
      }
      true
    }

  /** True to keep draining this conversation, false to stop until something re-kicks it. */
  private suspend fun send(message: PendingChatMessage): Boolean {
    val request = SendMessageRequest(clientMessageId = message.clientMessageId, body = message.body)
    return try {
      val sent = api.send(message.conversationId, request)
      // One transaction: the canonical row lands, the optimistic bubble retires, and the read
      // cursor advances past the sender's own message together.
      chatDao.landSentMessage(sent.toEntity())
      true
    } catch (cancellation: CancellationException) {
      // The drain was cancelled, not the send: leave the row claimed and let the next session's
      // resetInFlight() recover it.
      throw cancellation
    } catch (throwable: Throwable) {
      handleFailure(message, throwable)
    }
  }

  private suspend fun handleFailure(
    message: PendingChatMessage,
    throwable: Throwable
  ): Boolean {
    val error = throwable.toDomainError()
    // Bodies never reach the log — ids and error kind only.
    logger.w(
      tag = TAG,
      message = "Send failed for ${message.clientMessageId} in ${message.conversationId}: " +
        "${error::class.simpleName}, attempt ${message.attemptCount + 1}"
    )
    return when {
      // No network: not a delivery failure. Requeue without spending an attempt and stop; the
      // connectivity trigger re-kicks. Counting these would mark a merely-offline message Failed.
      error is DomainError.Offline -> {
        store.resetToQueued(message.conversationId, message.clientMessageId)
        false
      }
      // Server-side transient (5xx) with budget left: spend an attempt and stop so the message
      // keeps its place at the head; the caller retries with backoff.
      error is DomainError.Server && message.attemptCount + 1 < MAX_ATTEMPTS -> {
        store.requeue(message.conversationId, message.clientMessageId)
        false
      }
      // Permanent, or the transient budget is spent: park it and keep draining the rest — the user
      // decides what to do with it, and the queue must not wait on that.
      else -> {
        store.markFailed(message.conversationId, message.clientMessageId)
        true
      }
    }
  }

  private suspend fun mutexFor(conversationId: String): Mutex =
    mutexGuard.withLock { conversationMutexes.getOrPut(conversationId) { Mutex() } }

  private companion object {
    const val TAG = "ChatOutbox"

    /** Transient attempts before a message is parked as failed instead of blocking its conversation. */
    const val MAX_ATTEMPTS = 5
  }
}
