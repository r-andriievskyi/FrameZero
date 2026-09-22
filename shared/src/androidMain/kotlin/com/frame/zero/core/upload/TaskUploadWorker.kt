package com.frame.zero.core.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

class TaskUploadWorker(
  appContext: Context,
  params: WorkerParameters,
  private val uploadTask: UploadTaskUseCase,
  private val store: PendingUploadStore
) : CoroutineWorker(appContext, params) {
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

/**
 * WorkManager instantiates workers itself, so [TaskUploadWorker]'s dependencies arrive through
 * this factory rather than a service locator. `FrameZeroApp` installs it via
 * `Configuration.Provider`, which is why the manifest removes WorkManager's default initializer.
 *
 * [UploadTaskUseCase] is unscoped, so it is injected as a provider and a fresh one is built per
 * worker.
 */
@SingleIn(AppScope::class)
@Inject
class TaskUploadWorkerFactory(
  private val uploadTask: () -> UploadTaskUseCase,
  private val store: PendingUploadStore
) : WorkerFactory() {
  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters
  ): ListenableWorker? =
    if (workerClassName == TaskUploadWorker::class.java.name) {
      TaskUploadWorker(appContext, workerParameters, uploadTask(), store)
    } else {
      null
    }
}
