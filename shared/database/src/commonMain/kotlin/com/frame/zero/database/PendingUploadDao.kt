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

  @Upsert
  suspend fun upsert(entity: PendingUploadEntity)

  @Query("UPDATE pending_uploads SET status = :status WHERE uploadId = :uploadId")
  suspend fun updateStatus(
    uploadId: String,
    status: String
  )

  @Query("DELETE FROM pending_uploads WHERE uploadId = :uploadId")
  suspend fun delete(uploadId: String)
}
