package com.frame.zero.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

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

  /**
   * Upserts messages and advances the conversation's cached latestOrdinal in one transaction,
   * so every path that lands rows (socket, send, backfill, paging) keeps the unread counter in
   * step with the newest cached message. [entities] must all belong to one conversation.
   */
  @Transaction
  open suspend fun upsertMessagesAndAdvanceLatest(entities: List<MessageEntity>) {
    if (entities.isEmpty()) return
    upsertMessages(entities)
    advanceLatestOrdinal(entities.first().conversationId, entities.maxOf { it.ordinal })
  }

  /**
   * Same as [upsertMessagesAndAdvanceLatest], plus it drops the outbox rows the incoming messages
   * confirm — matched on `clientMessageId`, which the sender minted and the server echoes back.
   * Landing the canonical row and retiring the optimistic bubble in one transaction is what keeps
   * the list from flickering a duplicate.
   *
   * The delete lives here rather than on [ChatOutboxDao] because Room can only wrap one DAO's
   * methods in a single [Transaction].
   */
  @Transaction
  open suspend fun upsertMessagesAndClearPending(entities: List<MessageEntity>) {
    if (entities.isEmpty()) return
    upsertMessages(entities)
    advanceLatestOrdinal(entities.first().conversationId, entities.maxOf { it.ordinal })
    deletePendingMessages(entities.map { it.clientMessageId })
  }

  @Query("DELETE FROM chat_pending_messages WHERE clientMessageId IN (:clientMessageIds)")
  abstract suspend fun deletePendingMessages(clientMessageIds: List<String>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun upsertConversation(conversation: ConversationEntity)

  @Query("SELECT * FROM chat_conversations WHERE taskId = :taskId LIMIT 1")
  abstract suspend fun conversationByTaskId(taskId: String): ConversationEntity?

  /** Live conversation row for the unread badge; emits null until the chat is first opened. */
  @Query("SELECT * FROM chat_conversations WHERE taskId = :taskId LIMIT 1")
  abstract fun observeConversationByTaskId(taskId: String): Flow<ConversationEntity?>

  /** Forward-only: advances latestOrdinal as newer messages land, never rewinds it. */
  @Query(
    "UPDATE chat_conversations SET latestOrdinal = :ordinal " +
      "WHERE id = :conversationId AND :ordinal > latestOrdinal"
  )
  abstract suspend fun advanceLatestOrdinal(
    conversationId: String,
    ordinal: Long
  )

  /** Forward-only: advances the read cursor, never rewinds it. */
  @Query(
    "UPDATE chat_conversations SET lastReadOrdinal = :ordinal " +
      "WHERE id = :conversationId AND :ordinal > lastReadOrdinal"
  )
  abstract suspend fun advanceLastReadOrdinal(
    conversationId: String,
    ordinal: Long
  )

  @Query("DELETE FROM chat_messages")
  abstract suspend fun deleteAllMessages()

  @Query("DELETE FROM chat_conversations")
  abstract suspend fun deleteAllConversations()

  @Query("DELETE FROM chat_pending_messages")
  abstract suspend fun deleteAllPendingMessages()

  suspend fun clearAll() {
    deleteAllMessages()
    deleteAllConversations()
    deleteAllPendingMessages()
  }
}
