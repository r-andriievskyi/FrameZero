package com.frame.zero.repository.chat

import androidx.paging.PagingData
import com.frame.zero.domain.chat.ChatMessage
import com.frame.zero.domain.chat.Conversation
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
  suspend fun getOrCreateConversation(taskId: String): Conversation

  suspend fun cachedConversation(taskId: String): Conversation?

  fun messages(conversationId: String): Flow<PagingData<ChatMessage>>

  suspend fun subscribe(conversationId: String)

  suspend fun send(
    conversationId: String,
    clientMessageId: String,
    body: String
  )
}
