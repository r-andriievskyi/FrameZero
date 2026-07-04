package com.frame.zero.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * The cached task conversation, so reopening a chat resolves its conversation id offline
 * without re-hitting `GET /tasks/{id}/conversation`. One row per task (MVP is TASK-kind only).
 */
@Entity(
  tableName = "chat_conversations",
  indices = [Index(value = ["taskId"], unique = true)]
)
data class ConversationEntity(
  @PrimaryKey val id: String,
  val taskId: String,
  val productionId: String,
  val createdAtEpochMs: Long
)
