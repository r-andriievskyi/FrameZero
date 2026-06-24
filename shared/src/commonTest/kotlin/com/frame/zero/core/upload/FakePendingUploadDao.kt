package com.frame.zero.core.upload

import com.frame.zero.database.PendingUploadDao
import com.frame.zero.database.PendingUploadEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [PendingUploadDao] for tests; mirrors the Room DAO semantics. */
class FakePendingUploadDao : PendingUploadDao {
  private val rows = MutableStateFlow<List<PendingUploadEntity>>(emptyList())

  override fun observeAll(): Flow<List<PendingUploadEntity>> = rows

  override suspend fun get(uploadId: String): PendingUploadEntity? = rows.value.firstOrNull { it.uploadId == uploadId }

  override suspend fun upsert(entity: PendingUploadEntity) {
    rows.value = rows.value.filterNot { it.uploadId == entity.uploadId } + entity
  }

  override suspend fun updateStatus(
    uploadId: String,
    status: String
  ) {
    rows.value = rows.value.map { if (it.uploadId == uploadId) it.copy(status = status) else it }
  }

  override suspend fun delete(uploadId: String) {
    rows.value = rows.value.filterNot { it.uploadId == uploadId }
  }
}
