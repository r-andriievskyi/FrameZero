package com.frame.zero.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * The chat outbox: locally composed messages waiting to reach the server. An interface (not an
 * abstract class) so tests can drive the outbox against a hand-written fake, as `PendingUploadDao`
 * does.
 *
 * Rows are ordered by [PendingMessageEntity.createdAtEpochMs] within a conversation — the drain
 * takes them strictly one at a time so the server-assigned ordinals match compose order.
 */
@Dao
interface ChatOutboxDao {
  @Query("SELECT * FROM chat_pending_messages WHERE conversationId = :conversationId ORDER BY createdAtEpochMs ASC")
  fun observeByConversation(conversationId: String): Flow<List<PendingMessageEntity>>

  /** The head of the queue: oldest message still waiting to be sent, or null when drained. */
  @Query(
    "SELECT * FROM chat_pending_messages WHERE conversationId = :conversationId AND status = :status " +
      "ORDER BY createdAtEpochMs ASC LIMIT 1"
  )
  suspend fun nextQueued(
    conversationId: String,
    status: String
  ): PendingMessageEntity?

  /** Conversations with anything left to send, so a reconnect can drain them all. */
  @Query("SELECT DISTINCT conversationId FROM chat_pending_messages WHERE status = :status")
  suspend fun conversationsWithQueued(status: String): List<String>

  @Query("SELECT * FROM chat_pending_messages WHERE clientMessageId = :clientMessageId")
  suspend fun get(clientMessageId: String): PendingMessageEntity?

  @Upsert
  suspend fun upsert(entity: PendingMessageEntity)

  @Query("UPDATE chat_pending_messages SET status = :status WHERE clientMessageId = :clientMessageId")
  suspend fun updateStatus(
    clientMessageId: String,
    status: String
  )

  @Query(
    "UPDATE chat_pending_messages SET status = :status, attemptCount = attemptCount + 1 " +
      "WHERE clientMessageId = :clientMessageId"
  )
  suspend fun updateStatusAndCountAttempt(
    clientMessageId: String,
    status: String
  )

  @Query("DELETE FROM chat_pending_messages WHERE clientMessageId = :clientMessageId")
  suspend fun delete(clientMessageId: String)
}
