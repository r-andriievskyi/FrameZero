package com.frame.zero.feature.production.details

import com.frame.zero.core.upload.PendingUploadStore
import com.frame.zero.database.PendingUploadDao
import com.frame.zero.database.PendingUploadEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [PendingUploadDao]; mirrors the Room DAO semantics for a real [PendingUploadStore]
 *  without needing Room/SQLite in this module's tests. */
private class FakePendingUploadDao : PendingUploadDao {
  private val rows = MutableStateFlow<List<PendingUploadEntity>>(emptyList())

  override fun observeAll(): Flow<List<PendingUploadEntity>> = rows

  override suspend fun get(uploadId: String): PendingUploadEntity? = rows.value.firstOrNull { it.uploadId == uploadId }

  override suspend fun upsert(entity: PendingUploadEntity) {
    rows.value = rows.value.filterNot { it.uploadId == entity.uploadId } + entity
  }

  override suspend fun delete(uploadId: String) {
    rows.value = rows.value.filterNot { it.uploadId == uploadId }
  }
}

fun testPendingUploadStore(): PendingUploadStore = PendingUploadStore(FakePendingUploadDao())
