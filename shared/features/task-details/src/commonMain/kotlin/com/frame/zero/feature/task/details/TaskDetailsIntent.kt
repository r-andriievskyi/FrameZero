package com.frame.zero.feature.task.details

sealed interface TaskDetailsIntent {
  data object Refresh : TaskDetailsIntent

  data object MarkComplete : TaskDetailsIntent
}
