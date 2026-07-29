package com.frame.zero.core.upload

import com.frame.zero.database.PendingUploadDao
import com.frame.zero.database.PendingUploadEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * Persists in-flight background uploads in the shared Room database so they survive process
 * death and drive the UI's "uploading / failed" status. [PendingUploadEntity.status]/
 * [PendingUploadEntity.attemptCount]/[PendingUploadEntity.failureReason] are their own columns,
 * mutated only via [PendingUploadDao]'s atomic single-statement UPDATEs — [recordFailure] and
 * [markUploading] can race (a worker's failure landing the same moment as a user-triggered
 * retry) without either clobbering the other. [PendingUploadEntity.payload] holds everything
 * else and is written once at [add] time; it is never rewritten by a later mutation.
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
    dao.recordFailure(
      uploadId = uploadId,
      reason = reason.name,
      permanentReason = UploadFailureReason.Permanent.name,
      maxAttempts = MAX_UPLOAD_ATTEMPTS,
      failedStatus = PendingUploadStatus.Failed.name,
      uploadingStatus = PendingUploadStatus.Uploading.name
    )
    return get(uploadId)
  }

  /** Explicit user-initiated retry: a fresh attempt budget, not a continuation of the old one. */
  suspend fun markUploading(uploadId: String) {
    dao.markUploading(uploadId, PendingUploadStatus.Uploading.name)
  }

  suspend fun remove(uploadId: String) = dao.delete(uploadId)

  private fun PendingUploadEntity.toUpload(): PendingTaskUpload =
    json.decodeFromString<PendingTaskUpload>(payload)
      .copy(
        status = PendingUploadStatus.valueOf(status),
        attemptCount = attemptCount,
        failureReason = failureReason?.let(UploadFailureReason::valueOf)
      )

  private fun PendingTaskUpload.toEntity(): PendingUploadEntity =
    PendingUploadEntity(
      uploadId = uploadId,
      status = status.name,
      attemptCount = attemptCount,
      failureReason = failureReason?.name,
      payload = json.encodeToString(this)
    )
}
