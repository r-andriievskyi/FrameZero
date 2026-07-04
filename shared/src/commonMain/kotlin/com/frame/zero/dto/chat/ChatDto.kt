package com.frame.zero.dto.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Client copy of the chat wire DTOs. Duplicated from the server copy under the same
 * package names — edit both in the same change (see the wire-DTO duplication rule).
 *
 * [ConversationDto] is a sealed interface discriminated by `type`; `Task` is the only
 * subtype so far. A future `DIRECT` kind gets its own subtype rather than a nullable
 * `taskId`, so old clients tolerate the new payload shape.
 */
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
    val taskId: String
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
