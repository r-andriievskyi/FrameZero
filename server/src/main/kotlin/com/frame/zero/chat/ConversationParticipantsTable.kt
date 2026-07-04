package com.frame.zero.chat

import com.frame.zero.auth.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

object ConversationParticipantsTable : Table("conversation_participants") {
  val conversationId = javaUUID("conversation_id").references(ConversationsTable.id, onDelete = ReferenceOption.CASCADE)
  val userId = javaUUID("user_id").references(UsersTable.id)

  val lastReadOrdinal = long("last_read_ordinal")
  val joinedAt = timestamp("joined_at")

  override val primaryKey = PrimaryKey(conversationId, userId)

  init {
    index("idx_conversation_participants_user", false, userId)
  }
}
