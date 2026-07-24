package com.frame.zero.repository.chat.network

import com.frame.zero.dto.chat.ChatMessageDto
import com.frame.zero.dto.chat.ConversationDto
import com.frame.zero.dto.chat.SendMessageRequest
import com.frame.zero.dto.common.CursorPagedResponse
import kotlin.time.Instant

/**
 * [ChatApi] that records sends and assigns ordinals in arrival order — the server's actual
 * contract, and what makes out-of-order delivery observable in a test.
 */
internal class FakeChatApi : ChatApi {
  val sentBodies = mutableListOf<String>()

  /** Failures to raise, keyed by body; a body maps to the throwable its send should fail with. */
  var failures: MutableMap<String, Throwable> = mutableMapOf()

  private var nextOrdinal = 1L

  override suspend fun send(
    conversationId: String,
    request: SendMessageRequest
  ): ChatMessageDto {
    failures[request.body]?.let { throw it }
    sentBodies += request.body
    return ChatMessageDto(
      id = "server-${request.clientMessageId}",
      conversationId = conversationId,
      ordinal = nextOrdinal++,
      senderUserId = SENDER_ID,
      body = request.body,
      clientMessageId = request.clientMessageId,
      createdAt = Instant.fromEpochMilliseconds(0)
    )
  }

  override suspend fun getOrCreateConversation(taskId: String): ConversationDto = error("unused")

  override suspend fun listMessages(
    conversationId: String,
    before: Long?,
    limit: Int
  ): CursorPagedResponse<ChatMessageDto> = error("unused")

  override suspend fun markRead(
    conversationId: String,
    lastReadOrdinal: Long
  ): Long = lastReadOrdinal

  companion object {
    const val SENDER_ID = "me"
  }
}
