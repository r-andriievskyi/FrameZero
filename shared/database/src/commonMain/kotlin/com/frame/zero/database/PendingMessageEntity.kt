package com.frame.zero.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A composed-but-unsent chat message. Survives process death so an offline send is never lost.
 *
 * Queue position is [sequence], a monotonic insertion counter — never the wall clock, which ties on
 * same-millisecond sends and moves backwards on an NTP correction. [createdAtEpochMs] is display
 * data only.
 *
 * Uniqueness is `(conversationId, clientMessageId)`, matching the server's
 * `(conversation_id, sender_user_id, client_message_id)` index: the same client id in two
 * conversations is two messages, not a replay.
 */
@Entity(
  tableName = "chat_pending_messages",
  indices = [
    Index(value = ["conversationId", "clientMessageId"], unique = true),
    Index(value = ["conversationId", "sequence"])
  ]
)
data class PendingMessageEntity(
  @PrimaryKey(autoGenerate = true) val sequence: Long = 0,
  val clientMessageId: String,
  val conversationId: String,
  val body: String,
  val status: String,
  val attemptCount: Int,
  val createdAtEpochMs: Long
)
