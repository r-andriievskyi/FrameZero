package com.frame.zero.core.upload

/**
 * Schedules a task-create-with-attachment to upload in the background, surviving navigation
 * and (best-effort) process death. Android is backed by WorkManager, iOS by a background
 * `NSURLSession`. Each platform impl contributes itself to the graph, mirroring other
 * platform services; demo builds exclude both and bind their own.
 *
 * Implementations record the upload in [PendingUploadStore] and then hand it to the OS.
 */
interface TaskUploadScheduler {
  /** Records and starts the upload. */
  suspend fun enqueue(upload: PendingTaskUpload)

  /** Re-runs a previously failed upload. */
  suspend fun retry(uploadId: String)

  /** Cancels an upload and drops its pending record. */
  suspend fun cancel(uploadId: String)
}
