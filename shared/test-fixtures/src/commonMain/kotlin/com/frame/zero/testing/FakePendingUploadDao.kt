package com.frame.zero.testing

import com.frame.zero.core.upload.PendingUploadStore
import com.frame.zero.database.PendingUploadDao
import com.frame.zero.database.PendingUploadEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [PendingUploadDao]; mirrors the Room DAO semantics for a real [PendingUploadStore]
 *  without needing Room/SQLite in tests. */
class FakePendingUploadDao : PendingUploadDao {
  private val rows = MutableStateFlow<List<PendingUploadEntity>>(emptyList())

  override fun observeAll(): Flow<List<PendingUploadEntity>> = rows

  override suspend fun get(uploadId: String): PendingUploadEntity? = rows.value.firstOrNull { it.uploadId == uploadId }

  override suspend fun upsert(entity: PendingUploadEntity) {
    rows.value = rows.value.filterNot { it.uploadId == entity.uploadId } + entity
  }

  override suspend fun recordFailure(
    uploadId: String,
    reason: String,
    permanentReason: String,
    maxAttempts: Int,
    failedStatus: String,
    uploadingStatus: String
  ) {
    rows.value = rows.value.map { entity ->
      if (entity.uploadId != uploadId) {
        entity
      } else {
        val attemptCount = entity.attemptCount + 1
        val terminal = reason == permanentReason || attemptCount >= maxAttempts
        entity.copy(
          attemptCount = attemptCount,
          failureReason = reason,
          status = if (terminal) failedStatus else uploadingStatus
        )
      }
    }
  }

  override suspend fun markUploading(
    uploadId: String,
    status: String
  ) {
    rows.value = rows.value.map { entity ->
      if (entity.uploadId != uploadId) {
        entity
      } else {
        entity.copy(status = status, attemptCount = 0, failureReason = null)
      }
    }
  }

  override suspend fun delete(uploadId: String) {
    rows.value = rows.value.filterNot { it.uploadId == uploadId }
  }
}

fun testPendingUploadStore(): PendingUploadStore = PendingUploadStore(FakePendingUploadDao())
