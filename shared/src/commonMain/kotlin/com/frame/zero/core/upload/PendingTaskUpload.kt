package com.frame.zero.core.upload

import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskPriority
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
enum class PendingUploadStatus {
  Uploading,
  Failed
}

/**
 * A task-create-with-attachment that is being uploaded in the background. Persisted so it
 * survives process death (the upload itself is carried by WorkManager / a background
 * `NSURLSession`). [idempotencyKey] makes a retried upload safe on the server.
 */
@Serializable
data class PendingTaskUpload(
  val uploadId: String,
  val productionId: String,
  val title: String,
  val description: String? = null,
  val dueDate: LocalDate? = null,
  val assigneeUserId: String? = null,
  val priority: TaskPriority = TaskPriority.MEDIUM,
  val participantUserIds: List<String> = emptyList(),
  val fileName: String,
  val contentType: String,
  val localPath: String,
  val idempotencyKey: String,
  val status: PendingUploadStatus = PendingUploadStatus.Uploading
) {
  fun toCreateRequest(): CreateTaskRequest =
    CreateTaskRequest(
      productionId = productionId,
      title = title,
      description = description,
      dueDate = dueDate,
      assigneeUserId = assigneeUserId,
      priority = priority,
      participantUserIds = participantUserIds
    )
}
