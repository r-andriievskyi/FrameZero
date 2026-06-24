package com.frame.zero.core.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.frame.zero.domain.Outcome
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
    val succeeded = uploadTask(uploadId) is Outcome.Success
    return when {
      succeeded -> Result.success()
      runAttemptCount + 1 < MAX_ATTEMPTS -> Result.retry()
      else -> {
        store.markFailed(uploadId)
        Result.failure()
      }
    }
  }

  companion object {
    const val KEY_UPLOAD_ID = "uploadId"
    private const val MAX_ATTEMPTS = 4
  }
}
