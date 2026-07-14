package com.frame.zero.demo.upload

import com.frame.zero.core.upload.PendingTaskUpload
import com.frame.zero.core.upload.TaskUploadScheduler
import com.frame.zero.demo.data.DemoTasksRepository
import com.frame.zero.domain.task.NewTask
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

internal class DemoTaskUploadScheduler(
  private val tasksRepository: DemoTasksRepository
) : TaskUploadScheduler {
  override suspend fun enqueue(upload: PendingTaskUpload) {
    delay(UPLOAD_DELAY)
    tasksRepository.createTask(
      NewTask(
        productionId = upload.productionId,
        title = upload.title,
        description = upload.description,
        dueDate = upload.dueDate,
        assigneeUserId = upload.assigneeUserId,
        priority = upload.priority,
        participantUserIds = upload.participantUserIds
      )
    )
  }

  override suspend fun retry(uploadId: String) = Unit

  override suspend fun cancel(uploadId: String) = Unit

  private companion object {
    val UPLOAD_DELAY = 800.milliseconds
  }
}
