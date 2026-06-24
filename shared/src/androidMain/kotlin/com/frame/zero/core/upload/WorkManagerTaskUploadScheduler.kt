package com.frame.zero.core.upload

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class WorkManagerTaskUploadScheduler(
  private val context: Context,
  private val store: PendingUploadStore
) : TaskUploadScheduler {
  override suspend fun enqueue(upload: PendingTaskUpload) {
    store.add(upload)
    schedule(upload.uploadId)
  }

  override suspend fun retry(uploadId: String) {
    store.markUploading(uploadId)
    schedule(uploadId)
  }

  override suspend fun cancel(uploadId: String) {
    WorkManager.getInstance(context).cancelUniqueWork(workName(uploadId))
    store.remove(uploadId)
  }

  private fun schedule(uploadId: String) {
    val request = OneTimeWorkRequestBuilder<TaskUploadWorker>()
      .setInputData(workDataOf(TaskUploadWorker.KEY_UPLOAD_ID to uploadId))
      .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
      .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
      .build()
    WorkManager.getInstance(context)
      .enqueueUniqueWork(workName(uploadId), ExistingWorkPolicy.REPLACE, request)
  }

  private fun workName(uploadId: String): String = "$WORK_PREFIX$uploadId"

  private companion object {
    const val WORK_PREFIX = "task-upload-"
    const val BACKOFF_SECONDS = 10L
  }
}
