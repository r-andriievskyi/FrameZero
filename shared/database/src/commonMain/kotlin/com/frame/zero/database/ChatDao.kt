package com.frame.zero.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
abstract class ChatDao {
  @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY ordinal DESC")
  abstract fun messagesPagingSource(conversationId: String): PagingSource<Int, MessageEntity>

  @Query("SELECT MIN(ordinal) FROM chat_messages WHERE conversationId = :conversationId")
  abstract suspend fun minOrdinal(conversationId: String): Long?

  @Query("SELECT MAX(ordinal) FROM chat_messages WHERE conversationId = :conversationId")
  abstract suspend fun maxOrdinal(conversationId: String): Long?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun upsertMessages(entities: List<MessageEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun upsertConversation(conversation: ConversationEntity)

  @Query("SELECT * FROM chat_conversations WHERE taskId = :taskId LIMIT 1")
  abstract suspend fun conversationByTaskId(taskId: String): ConversationEntity?

  @Query("DELETE FROM chat_messages")
  abstract suspend fun deleteAllMessages()

  @Query("DELETE FROM chat_conversations")
  abstract suspend fun deleteAllConversations()

  suspend fun clearAll() {
    deleteAllMessages()
    deleteAllConversations()
  }
}
