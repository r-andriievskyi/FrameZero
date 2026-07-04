package com.frame.zero.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A cached chat message. Ordered within a conversation by the server-assigned [ordinal]
 * (never timestamps). The `(conversationId, ordinal)` pair is unique; the PK is the server
 * id so a message arriving twice (REST send response + WS broadcast) upserts to one row.
 */
@Entity(
  tableName = "chat_messages",
  indices = [Index(value = ["conversationId", "ordinal"], unique = true)]
)
data class MessageEntity(
  @PrimaryKey val id: String,
  val conversationId: String,
  val ordinal: Long,
  val senderUserId: String,
  val body: String,
  val clientMessageId: String,
  val createdAtEpochMs: Long
)
