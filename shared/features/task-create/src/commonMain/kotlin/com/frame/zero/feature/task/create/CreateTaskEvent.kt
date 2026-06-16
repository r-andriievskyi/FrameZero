package com.frame.zero.feature.task.create

sealed interface CreateTaskEvent {
  data class Created(
    val taskId: String
  ) : CreateTaskEvent
}
