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
   */
  @Transaction
  open suspend fun upsertMessagesAndClearPending(entities: List<MessageEntity>) {
    if (entities.isEmpty()) return
    upsertMessagesAndAdvanceLatest(entities)
    deletePendingMessages(entities.first().conversationId, entities.map { it.clientMessageId })
  }

  /**
   * Scoped to one conversation: this runs off the socket stream, which carries other people's
   * messages, and client ids are only unique within a conversation.
   */
  @Query(
    "DELETE FROM chat_pending_messages " +
      "WHERE conversationId = :conversationId AND clientMessageId IN (:clientMessageIds)"
  )
  abstract suspend fun deletePendingMessages(
    conversationId: String,
    clientMessageIds: List<String>
  )

  /**
   * Lands the user's own just-sent message: the canonical row replaces its outbox row, the cached
   * latest ordinal advances, and — because the sender has by definition read what they wrote — the
   * read cursor advances too, all in one transaction so a crash can never leave your own message
   * counting as unread.
   */
  @Transaction
  open suspend fun landSentMessage(entity: MessageEntity) {
    upsertMessages(listOf(entity))
    advanceLatestOrdinal(entity.conversationId, entity.ordinal)
    deletePendingMessages(entity.conversationId, listOf(entity.clientMessageId))
    advanceLastReadOrdinal(entity.conversationId, entity.ordinal)
  }

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun upsertConversation(conversation: ConversationEntity)

  @Query("SELECT * FROM chat_conversations WHERE taskId = :taskId LIMIT 1")
  abstract suspend fun conversationByTaskId(taskId: String): ConversationEntity?

  @Query("SELECT * FROM chat_conversations WHERE taskId = :taskId LIMIT 1")
  abstract fun observeConversationByTaskId(taskId: String): Flow<ConversationEntity?>

  @Query(
    "UPDATE chat_conversations SET latestOrdinal = :ordinal " +
      "WHERE id = :conversationId AND :ordinal > latestOrdinal"
  )
  abstract suspend fun advanceLatestOrdinal(
    conversationId: String,
    ordinal: Long
  )

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
