package com.frame.zero.repository.chat.network

import com.frame.zero.dto.chat.ChatMessageDto
import com.frame.zero.dto.chat.ConversationDto
import com.frame.zero.dto.chat.SendMessageRequest
import com.frame.zero.dto.common.CursorPagedResponse

interface ChatApi {
  suspend fun getOrCreateConversation(taskId: String): ConversationDto

  suspend fun listMessages(
    conversationId: String,
    before: Long?,
    limit: Int
  ): CursorPagedResponse<ChatMessageDto>

  suspend fun send(
    conversationId: String,
    request: SendMessageRequest
  ): ChatMessageDto

  suspend fun markRead(
    conversationId: String,
    lastReadOrdinal: Long
  ): Long
}
