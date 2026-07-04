package com.frame.zero.domain.chat

import kotlin.time.Instant

/** A single chat message, ordered within its conversation by the server-assigned [ordinal]. */
data class ChatMessage(
  val id: String,
  val conversationId: String,
  val ordinal: Long,
  val senderUserId: String,
  val body: String,
  val clientMessageId: String,
  val createdAt: Instant
)

/** A task-scoped conversation. The MVP only has TASK-kind conversations. */
data class Conversation(
  val id: String,
  val taskId: String,
  val productionId: String,
  val createdAt: Instant
)
