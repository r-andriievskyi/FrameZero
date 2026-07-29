package com.frame.zero.testing

import com.frame.zero.core.upload.PendingTaskUpload
import com.frame.zero.core.upload.TaskUploadScheduler

class FakeTaskUploadScheduler : TaskUploadScheduler {
  val enqueued: MutableList<PendingTaskUpload> = mutableListOf()
  val retried: MutableList<String> = mutableListOf()
  val cancelled: MutableList<String> = mutableListOf()

  override suspend fun enqueue(upload: PendingTaskUpload) {
    enqueued += upload
  }

  override suspend fun retry(uploadId: String) {
    retried += uploadId
  }

  override suspend fun cancel(uploadId: String) {
    cancelled += uploadId
  }
}
