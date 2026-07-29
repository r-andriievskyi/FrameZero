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

  /**
   * Records one failed attempt: increments [PendingTaskUpload.attemptCount] and sets
   * [PendingTaskUpload.failureReason]. Terminal ([PendingUploadStatus.Failed]) once [reason] is
   * [UploadFailureReason.Permanent] or the attempt budget ([MAX_UPLOAD_ATTEMPTS]) is spent;
   * otherwise stays [PendingUploadStatus.Uploading] so the caller knows there's budget to retry.
   * Returns the updated record (null if [uploadId] no longer exists — e.g. the user cancelled it
   * mid-request).
   */
  suspend fun recordFailure(
    uploadId: String,
    reason: UploadFailureReason
  ): PendingTaskUpload? {
    val current = get(uploadId) ?: return null
    val attemptCount = current.attemptCount + 1
    val terminal = reason == UploadFailureReason.Permanent || attemptCount >= MAX_UPLOAD_ATTEMPTS
    val updated = current.copy(
      attemptCount = attemptCount,
      failureReason = reason,
      status = if (terminal) PendingUploadStatus.Failed else PendingUploadStatus.Uploading
    )
    dao.upsert(updated.toEntity())
    return updated
  }

  /** Explicit user-initiated retry: a fresh attempt budget, not a continuation of the old one. */
  suspend fun markUploading(uploadId: String) {
    val current = get(uploadId) ?: return
    dao.upsert(
      current.copy(status = PendingUploadStatus.Uploading, attemptCount = 0, failureReason = null).toEntity()
    )
  }

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
