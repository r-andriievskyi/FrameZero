package com.frame.zero.feature.task.create

sealed interface CreateTaskEvent {
  data class Created(
    val taskId: String
  ) : CreateTaskEvent

  /** A task with an attachment was handed to the background uploader; just navigate back. */
  data object UploadEnqueued : CreateTaskEvent
}
