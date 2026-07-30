package com.frame.zero.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingUploadDao {
  @Query("SELECT * FROM pending_uploads")
  fun observeAll(): Flow<List<PendingUploadEntity>>

  @Query("SELECT * FROM pending_uploads WHERE uploadId = :uploadId")
  suspend fun get(uploadId: String): PendingUploadEntity?

  /** Initial insert only — [recordFailure]/[markUploading] own every later mutation and never
   *  touch [PendingUploadEntity.payload], so this is the one call site allowed to upsert it. */
  @Upsert
  suspend fun upsert(entity: PendingUploadEntity)

  /**
   * Single atomic UPDATE: increments [PendingUploadEntity.attemptCount], records [reason], and
   * derives the resulting [PendingUploadEntity.status] in the same statement — a worker's
   * failure and a user-triggered [markUploading] can never interleave into a lost update, since
   * neither is a read-then-write from Kotlin.
   */
  @Query(
    "UPDATE pending_uploads SET " +
      "attemptCount = attemptCount + 1, " +
      "failureReason = :reason, " +
      "status = CASE WHEN :reason = :permanentReason OR attemptCount + 1 >= :maxAttempts " +
      "THEN :failedStatus ELSE :uploadingStatus END " +
      "WHERE uploadId = :uploadId"
  )
  suspend fun recordFailure(
    uploadId: String,
    reason: String,
    permanentReason: String,
    maxAttempts: Int,
    failedStatus: String,
    uploadingStatus: String
  )

  /** Explicit user-initiated retry: a fresh attempt budget, atomically. */
  @Query(
    "UPDATE pending_uploads SET status = :status, attemptCount = 0, failureReason = NULL WHERE uploadId = :uploadId"
  )
  suspend fun markUploading(
    uploadId: String,
    status: String
  )

  @Query("DELETE FROM pending_uploads WHERE uploadId = :uploadId")
  suspend fun delete(uploadId: String)
}
