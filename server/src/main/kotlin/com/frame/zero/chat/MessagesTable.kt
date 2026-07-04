package com.frame.zero.chat

import com.frame.zero.auth.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

object MessagesTable : Table("messages") {
  val id = javaUUID("id")

  // CASCADE: message history dies with its conversation.
  val conversationId = javaUUID("conversation_id").references(ConversationsTable.id, onDelete = ReferenceOption.CASCADE)

  // Server-assigned monotonic counter per conversation; unique per conversation.
  val ordinal = long("ordinal")
  val senderUserId = javaUUID("sender_user_id").references(UsersTable.id)
  val body = text("body")

  // Client-generated idempotency token, unique per conversation+sender.
  val clientMessageId = varchar("client_message_id", 64)
  val createdAt = timestamp("created_at")

  override val primaryKey = PrimaryKey(id)

  init {
    uniqueIndex("messages_conversation_ordinal_unique", conversationId, ordinal)
    uniqueIndex("messages_conv_sender_client_id_unique", conversationId, senderUserId, clientMessageId)
  }
}
