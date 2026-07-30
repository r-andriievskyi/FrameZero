package com.frame.zero.core.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TaskUploadWorker(
  appContext: Context,
  params: WorkerParameters
) : CoroutineWorker(appContext, params),
  KoinComponent {
  private val uploadTask: UploadTaskUseCase by inject()
  private val store: PendingUploadStore by inject()

  override suspend fun doWork(): Result {
    val uploadId = inputData.getString(KEY_UPLOAD_ID) ?: return Result.failure()
    uploadTask(uploadId)
    // UploadTaskUseCase never throws — success removes the record, failure classifies and
    // records it — so the record's current state is the single source of truth for what
    // WorkManager should do next, rather than WorkManager's own runAttemptCount (which resets
    // on every fresh enqueue and can't see an explicit user-triggered retry).
    val current = store.get(uploadId)
    return when {
      current == null -> Result.success()
      current.status == PendingUploadStatus.Failed -> Result.failure()
      else -> Result.retry()
    }
  }

  companion object {
    const val KEY_UPLOAD_ID = "uploadId"
  }
}
