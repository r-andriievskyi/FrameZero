package com.frame.zero.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * The chat outbox: locally composed messages waiting to reach the server. An interface (not an
 * abstract class) so tests can drive the outbox against a hand-written fake, as `PendingUploadDao`
 * does.
 *
 * The one-at-a-time-per-conversation rule that keeps server ordinals in compose order is enforced
 * here, not just in the drain's memory: [nextQueued] hides a conversation that already has a
 * message in flight, and [claim] is a compare-and-set, so two overlapping drains cannot both take
 * the same row.
 *
 * Rows are addressed by `(conversationId, clientMessageId)` — the same client id in two
 * conversations is two distinct messages, as it is server-side.
 */
@Dao
interface ChatOutboxDao {
  @Query("SELECT * FROM chat_pending_messages WHERE conversationId = :conversationId ORDER BY sequence ASC")
  fun observeByConversation(conversationId: String): Flow<List<PendingMessageEntity>>

  /** Head of the queue, or null when the conversation is drained or already has a message in flight. */
  @Query(
    "SELECT * FROM chat_pending_messages WHERE conversationId = :conversationId AND status = :queued " +
      "AND NOT EXISTS (" +
      "SELECT 1 FROM chat_pending_messages WHERE conversationId = :conversationId AND status = :sending" +
      ") ORDER BY sequence ASC LIMIT 1"
  )
  suspend fun nextQueued(
    conversationId: String,
    queued: String,
    sending: String
  ): PendingMessageEntity?

  /** Returns rows changed: 0 means another drain claimed it first and this caller must not send. */
  @Query(
    "UPDATE chat_pending_messages SET status = :sending " +
      "WHERE conversationId = :conversationId AND clientMessageId = :clientMessageId AND status = :queued"
  )
  suspend fun claim(
    conversationId: String,
    clientMessageId: String,
    queued: String,
    sending: String
  ): Int

  /**
   * Returns this conversation's rows stranded in flight (a drain killed or cancelled mid-send) to
   * the queue. The send is not known to have failed — the server may have persisted it — but
   * `clientMessageId` idempotency makes a resend harmless, while leaving the row in flight strands
   * it forever. The drain runs this under its per-conversation lock, so it never touches a live send.
   */
  @Query(
    "UPDATE chat_pending_messages SET status = :queued " +
      "WHERE conversationId = :conversationId AND status = :sending"
  )
  suspend fun resetInFlight(
    conversationId: String,
    queued: String,
    sending: String
  ): Int

  /**
   * Conversations with anything still to deliver — queued *or* stranded in flight — so a flush
   * covers rows a killed process left mid-send, which the drain then recovers under its lock.
   */
  @Query(
    "SELECT DISTINCT conversationId FROM chat_pending_messages " +
      "WHERE status = :queued OR status = :sending ORDER BY conversationId"
  )
  suspend fun conversationsWithPending(
    queued: String,
    sending: String
  ): List<String>

  @Query(
    "SELECT * FROM chat_pending_messages " +
      "WHERE conversationId = :conversationId AND clientMessageId = :clientMessageId"
  )
  suspend fun get(
    conversationId: String,
    clientMessageId: String
  ): PendingMessageEntity?

  /** Ignores a message already queued: a re-enqueue must not rewind its position, status or attempts. */
  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(entity: PendingMessageEntity)

  @Query(
    "UPDATE chat_pending_messages SET status = :status " +
      "WHERE conversationId = :conversationId AND clientMessageId = :clientMessageId"
  )
  suspend fun updateStatus(
    conversationId: String,
    clientMessageId: String,
    status: String
  )

  @Query(
    "UPDATE chat_pending_messages SET status = :status, attemptCount = attemptCount + 1 " +
      "WHERE conversationId = :conversationId AND clientMessageId = :clientMessageId"
  )
  suspend fun updateStatusAndCountAttempt(
    conversationId: String,
    clientMessageId: String,
    status: String
  )

  @Query(
    "DELETE FROM chat_pending_messages " +
      "WHERE conversationId = :conversationId AND clientMessageId = :clientMessageId"
  )
  suspend fun delete(
    conversationId: String,
    clientMessageId: String
  )
}
