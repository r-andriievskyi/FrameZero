package com.frame.zero.testing

import androidx.paging.PagingData
import com.frame.zero.domain.chat.ChatMessage
import com.frame.zero.domain.chat.Conversation
import com.frame.zero.repository.chat.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Only [observeConversation]/[cachedConversation] carry state; the rest are inert. */
class FakeChatRepository(
  private val conversation: Conversation? = null
) : ChatRepository {
  override suspend fun getOrCreateConversation(taskId: String): Conversation = error("unused")

  override suspend fun cachedConversation(taskId: String): Conversation? = conversation

  override fun observeConversation(taskId: String): Flow<Conversation?> = flowOf(conversation)

  override fun messages(conversationId: String): Flow<PagingData<ChatMessage>> = flowOf(PagingData.empty())

  override suspend fun subscribe(conversationId: String) = Unit

  override suspend fun send(
    conversationId: String,
    clientMessageId: String,
    body: String
  ) = Unit

  override suspend fun markRead(
    conversationId: String,
    lastReadOrdinal: Long
  ) = Unit
}
