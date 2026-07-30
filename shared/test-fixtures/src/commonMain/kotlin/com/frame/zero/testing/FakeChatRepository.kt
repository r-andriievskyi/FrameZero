package com.frame.zero.testing

import androidx.paging.PagingData
import com.frame.zero.domain.chat.ChatMessage
import com.frame.zero.domain.chat.Conversation
import com.frame.zero.domain.chat.PendingChatMessage
import com.frame.zero.repository.chat.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * [observeConversation]/[cachedConversation] answer from the constructor; the outbox calls are
 * recorded, and [pending] is writable so a test can drive the optimistic-bubble state.
 */
class FakeChatRepository(
  private val conversation: Conversation? = null
) : ChatRepository {
  /** Queued sends, in order, as `(conversationId, clientMessageId, body)`. */
  val enqueued = mutableListOf<Triple<String, String, String>>()

  /** Retried pending messages, as `(conversationId, clientMessageId)`. */
  val retried = mutableListOf<Pair<String, String>>()

  /** Discarded pending messages, as `(conversationId, clientMessageId)`. */
  val discarded = mutableListOf<Pair<String, String>>()

  val pending = MutableStateFlow<List<PendingChatMessage>>(emptyList())

  /** Set to make queueing fail, standing in for a broken local database. */
  var enqueueFailure: Throwable? = null

  val connectionState = MutableStateFlow(true)
  override val isConnected: Flow<Boolean> = connectionState

  override suspend fun getOrCreateConversation(taskId: String): Conversation =
    conversation ?: error("no conversation configured")

  override suspend fun cachedConversation(taskId: String): Conversation? = conversation

  override fun observeConversation(taskId: String): Flow<Conversation?> = flowOf(conversation)

  override fun messages(conversationId: String): Flow<PagingData<ChatMessage>> = flowOf(PagingData.empty())

  override suspend fun subscribe(conversationId: String) = Unit

  override suspend fun enqueue(
    conversationId: String,
    clientMessageId: String,
    body: String
  ) {
    enqueueFailure?.let { throw it }
    enqueued += Triple(conversationId, clientMessageId, body)
  }

  override fun observePending(conversationId: String): Flow<List<PendingChatMessage>> = pending

  override suspend fun retryPending(
    conversationId: String,
    clientMessageId: String
  ) {
    retried += conversationId to clientMessageId
  }

  override suspend fun discardPending(
    conversationId: String,
    clientMessageId: String
  ) {
    discarded += conversationId to clientMessageId
  }

  override suspend fun flushOutbox() = Unit

  override suspend fun markRead(
    conversationId: String,
    lastReadOrdinal: Long
  ) = Unit
}
