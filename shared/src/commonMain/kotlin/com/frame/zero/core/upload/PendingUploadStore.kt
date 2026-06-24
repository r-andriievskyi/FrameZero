package com.frame.zero.core.upload

import com.frame.zero.database.PendingUploadDao
import com.frame.zero.database.PendingUploadEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * Persists in-flight background uploads in the shared Room database so they survive process
 * death and drive the UI's "uploading / failed" status. Mutations are atomic DAO ops; [status]
 * lives in its own column so it can be flipped without rewriting the payload.
 */
class PendingUploadStore(
  private val dao: PendingUploadDao,
  private val json: Json = Json { ignoreUnknownKeys = true }
) {
  val uploads: Flow<List<PendingTaskUpload>> =
    dao.observeAll().map { rows -> rows.map { it.toUpload() } }

  suspend fun get(uploadId: String): PendingTaskUpload? = dao.get(uploadId)?.toUpload()

  suspend fun add(upload: PendingTaskUpload) = dao.upsert(upload.toEntity())

  suspend fun markFailed(uploadId: String) = dao.updateStatus(uploadId, PendingUploadStatus.Failed.name)

  suspend fun markUploading(uploadId: String) = dao.updateStatus(uploadId, PendingUploadStatus.Uploading.name)

  suspend fun remove(uploadId: String) = dao.delete(uploadId)

  private fun PendingUploadEntity.toUpload(): PendingTaskUpload =
    json.decodeFromString<PendingTaskUpload>(payload)
      .copy(status = PendingUploadStatus.valueOf(status))

  private fun PendingTaskUpload.toEntity(): PendingUploadEntity =
    PendingUploadEntity(
      uploadId = uploadId,
      status = status.name,
      payload = json.encodeToString(this)
    )
}
