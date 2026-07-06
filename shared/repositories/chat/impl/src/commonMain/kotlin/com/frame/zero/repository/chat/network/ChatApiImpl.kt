package com.frame.zero.repository.chat.network

import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.dto.chat.ChatMessageDto
import com.frame.zero.dto.chat.ConversationDto
import com.frame.zero.dto.chat.MarkReadRequest
import com.frame.zero.dto.chat.MarkReadResponse
import com.frame.zero.dto.chat.SendMessageRequest
import com.frame.zero.dto.common.CursorPagedResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class ChatApiImpl(
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig
) : ChatApi {
  override suspend fun getOrCreateConversation(taskId: String): ConversationDto =
    httpClient.get(
      "${networkConfig.baseUrl}/api/v1/tasks/$taskId/conversation"
    ).body()

  override suspend fun listMessages(
    conversationId: String,
    before: Long?,
    limit: Int
  ): CursorPagedResponse<ChatMessageDto> =
    httpClient.get(
      "${networkConfig.baseUrl}/api/v1/conversations/$conversationId/messages"
    ) {
      before?.let { parameter("before", it) }
      parameter("limit", limit)
    }.body()

  override suspend fun send(
    conversationId: String,
    request: SendMessageRequest
  ): ChatMessageDto =
    httpClient.post(
      "${networkConfig.baseUrl}/api/v1/conversations/$conversationId/messages"
    ) { setBody(request) }.body()

  override suspend fun markRead(
    conversationId: String,
    lastReadOrdinal: Long
  ): Long =
    httpClient.put(
      "${networkConfig.baseUrl}/api/v1/conversations/$conversationId/read"
    ) { setBody(MarkReadRequest(lastReadOrdinal)) }
      .body<MarkReadResponse>()
      .lastReadOrdinal
}
