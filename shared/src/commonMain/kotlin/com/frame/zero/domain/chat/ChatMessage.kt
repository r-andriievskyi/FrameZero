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
  val createdAt: Instant,
  /** Highest message [ChatMessage.ordinal] known for this conversation, 0 when empty. */
  val latestOrdinal: Long,
  /** This user's read cursor; messages with a higher ordinal are unread. */
  val lastReadOrdinal: Long
) {
  /** Unread = everything past the read cursor. Derived here, never a wire field. */
  val unreadCount: Long
    get() = (latestOrdinal - lastReadOrdinal).coerceAtLeast(0)
}
