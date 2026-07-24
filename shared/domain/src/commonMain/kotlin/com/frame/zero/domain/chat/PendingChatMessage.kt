package com.frame.zero.domain.chat

import kotlin.time.Instant

/**
 * A message composed locally and not yet acknowledged by the server. Identified by the
 * client-generated [clientMessageId], which is reused across every retry so the server dedupes a
 * resend instead of persisting a duplicate.
 *
 * Has no `ordinal`: ordering is assigned by the server on arrival, so pending messages sort by
 * [createdAt] among themselves and always render after every confirmed [ChatMessage].
 */
data class PendingChatMessage(
  val clientMessageId: String,
  val conversationId: String,
  val body: String,
  val status: PendingMessageStatus,
  /** Delivery attempts so far; past a cap the drain gives up rather than blocking the queue. */
  val attemptCount: Int,
  val createdAt: Instant
)

enum class PendingMessageStatus {
  Queued,

  Sending,

  Failed
}
