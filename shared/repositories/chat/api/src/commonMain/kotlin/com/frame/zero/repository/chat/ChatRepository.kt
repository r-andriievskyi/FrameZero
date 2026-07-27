package com.frame.zero.repository.chat

import androidx.paging.PagingData
import com.frame.zero.domain.chat.ChatMessage
import com.frame.zero.domain.chat.Conversation
import com.frame.zero.domain.chat.PendingChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
  suspend fun getOrCreateConversation(taskId: String): Conversation

  suspend fun cachedConversation(taskId: String): Conversation?

  /** Live conversation (with read state) for a task; emits null until the chat is first opened. */
  fun observeConversation(taskId: String): Flow<Conversation?>

  fun messages(conversationId: String): Flow<PagingData<ChatMessage>>

  suspend fun subscribe(conversationId: String)

  /**
   * Queues a message and returns as soon as it is stored locally — sending never waits on the
   * network, and a queued message survives process death. Delivery is one at a time per
   * conversation, so server ordinals follow compose order. [clientMessageId] is the caller-owned
   * idempotency key, reused on every retry.
   */
  suspend fun enqueue(
    conversationId: String,
    clientMessageId: String,
    body: String
  )

  /** Messages composed but not yet acknowledged, oldest first. Empty once everything is delivered. */
  fun observePending(conversationId: String): Flow<List<PendingChatMessage>>

  /** Re-queues a failed message, keeping its id so the resend is deduped rather than duplicated. */
  suspend fun retryPending(
    conversationId: String,
    clientMessageId: String
  )

  /** Drops a pending message for good; it is never sent. */
  suspend fun discardPending(
    conversationId: String,
    clientMessageId: String
  )

  /** Flushes every conversation with queued messages. Called at app start and on reconnect. */
  suspend fun flushOutbox()

  /** Advances the read cursor for [conversationId] to [lastReadOrdinal] (server-clamped). */
  suspend fun markRead(
    conversationId: String,
    lastReadOrdinal: Long
  )
}
