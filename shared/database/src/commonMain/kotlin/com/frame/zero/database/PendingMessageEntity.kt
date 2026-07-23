package com.frame.zero.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A composed-but-unsent chat message. Survives process death so an offline send is never lost.
 *
 * The PK is the client-generated id, so re-enqueueing the same message is idempotent locally the
 * same way it is server-side. Unlike `pending_uploads` the payload is spread over columns rather
 * than an opaque JSON blob: the outbox drain queries by conversation and status, and flips status
 * with a single UPDATE.
 */
@Entity(
  tableName = "chat_pending_messages",
  indices = [Index(value = ["conversationId", "createdAtEpochMs"])]
)
data class PendingMessageEntity(
  @PrimaryKey val clientMessageId: String,
  val conversationId: String,
  val body: String,
  val status: String,
  val attemptCount: Int,
  val createdAtEpochMs: Long
)
