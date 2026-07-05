package com.frame.zero.dto.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
sealed interface ConversationDto {
  val id: String
  val productionId: String
  val createdAt: Instant

  @Serializable
  @SerialName("task")
  data class Task(
    override val id: String,
    override val productionId: String,
    override val createdAt: Instant,
    val taskId: String,
    // Read-state fields: latest = MAX(ordinal) (0 when empty), lastRead = this caller's
    // last_read_ordinal (0 when they've read nothing). The client derives the unread
    // count itself — unread is a UI hint, not a wire field.
    val latestOrdinal: Long,
    val lastReadOrdinal: Long
  ) : ConversationDto
}

@Serializable
data class ChatMessageDto(
  val id: String,
  val conversationId: String,
  val ordinal: Long,
  val senderUserId: String,
  val body: String,
  val clientMessageId: String,
  val createdAt: Instant
)

@Serializable
data class SendMessageRequest(
  val clientMessageId: String,
  val body: String
)

@Serializable
data class MarkReadRequest(
  val lastReadOrdinal: Long
)

/** The read cursor the server actually applied after forward-only clamping. */
@Serializable
data class MarkReadResponse(
  val lastReadOrdinal: Long
)
