package com.frame.zero.repository.chat.outbox

import com.frame.zero.core.logging.Logger
import com.frame.zero.core.network.connectivity.ConnectivityObserver
import com.frame.zero.database.ChatDao
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.chat.PendingChatMessage
import com.frame.zero.domain.toDomainError
import com.frame.zero.dto.chat.SendMessageRequest
import com.frame.zero.repository.chat.local.toEntity
import com.frame.zero.repository.chat.network.ChatApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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
 *
 * Owns the coroutines that do the sending, so [shutdown] can stop them at sign-out before the
 * tables are wiped: a send left in flight would otherwise write the signed-out user's message into
 * the freshly cleared database.
 */
internal class ChatOutbox(
  private val store: ChatOutboxStore,
  private val api: ChatApi,
  private val chatDao: ChatDao,
  private val scheduler: ChatOutboxScheduler,
  private val connectivityObserver: ConnectivityObserver,
  private val logger: Logger,
  private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
  private val conversationMutexes = mutableMapOf<String, Mutex>()
  private val retryLoops = mutableMapOf<String, Job>()
  private val guard = Mutex()

  /**
   * Starts delivering [conversationId] in the background, or does nothing if a delivery loop for it
   * is already running. Single-flight matters: every loop that runs shares one retry budget per
   * message, so N loops would spend it N times faster and park a message on a transient blip.
   */
  suspend fun kick(conversationId: String) {
    guard.withLock {
      if (retryLoops[conversationId]?.isActive == true) return
      retryLoops[conversationId] = scope.launch { drainWithRetry(conversationId) }
    }
  }

  /**
   * Flushes every conversation with anything pending — including rows a killed process left in
   * flight, which [ChatOutboxStore.conversationsWithPending] surfaces and [drain] then recovers.
   * Used on reconnect, sign-in and app start.
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

  /** Stops all delivery and forgets per-conversation state. Sign-out must await this before wiping. */
  suspend fun shutdown() {
    scope.coroutineContext[Job]?.children?.toList()?.forEach { it.cancelAndJoin() }
    guard.withLock {
      retryLoops.clear()
      conversationMutexes.clear()
    }
  }

  /**
   * Drains a conversation and, on a server-side transient failure while still online, retries with
   * backoff. Without this an iOS send (no WorkManager backstop there) would sit on a 5xx until the
   * next app launch, since neither the connectivity nor the socket-reconnect trigger fires while
   * the network stays up. An offline stop hands over to the platform backstop and the connectivity
   * trigger; the drain itself parks a message once its retry budget is spent, so this terminates.
   */
  private suspend fun drainWithRetry(conversationId: String) {
    var backoff = INITIAL_RETRY_DELAY
    while (true) {
      if (drain(conversationId)) return
      // Only now is a durable retry worth its wakeup: the in-process drain could not finish.
      scheduler.schedule(conversationId)
      if (!connectivityObserver.isCurrentlyOnline()) return
      delay(backoff)
      backoff = (backoff * 2).coerceAtMost(MAX_RETRY_DELAY)
    }
  }

  /** True to keep draining this conversation, false to stop until something re-kicks it. */
  private suspend fun send(message: PendingChatMessage): Boolean {
    val request = SendMessageRequest(clientMessageId = message.clientMessageId, body = message.body)
    val sent = try {
      api.send(message.conversationId, request)
    } catch (cancellation: CancellationException) {
      // The drain was cancelled, not the send: leave the row claimed and let the next session's
      // per-conversation recovery pick it up.
      throw cancellation
    } catch (throwable: Throwable) {
      return handleFailure(message, throwable)
    }

    return try {
      // One transaction: the canonical row lands, the optimistic bubble retires, and the read
      // cursor advances past the sender's own message together.
      chatDao.landSentMessage(sent.toEntity())
      true
    } catch (cancellation: CancellationException) {
      throw cancellation
    } catch (throwable: Throwable) {
      // Delivered, but not recorded. Deliberately not treated as a send failure: parking a message
      // everyone else already received would be a lie. Requeue without spending an attempt — the
      // resend is deduped server-side and lands the same row.
      logger.w(tag = TAG, message = "Could not record sent message ${message.clientMessageId}", throwable = throwable)
      store.resetToQueued(message.conversationId, message.clientMessageId)
      false
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
      // keeps its place at the head; the retry loop comes back with backoff.
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
    guard.withLock { conversationMutexes.getOrPut(conversationId) { Mutex() } }

  private companion object {
    const val TAG = "ChatOutbox"

    /** Transient attempts before a message is parked as failed instead of blocking its conversation. */
    const val MAX_ATTEMPTS = 5

    val INITIAL_RETRY_DELAY: Duration = 2.seconds
    val MAX_RETRY_DELAY: Duration = 30.seconds
  }
}
