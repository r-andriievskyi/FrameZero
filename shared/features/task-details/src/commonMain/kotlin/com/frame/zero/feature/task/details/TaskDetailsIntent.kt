package com.frame.zero.feature.task.details

sealed interface TaskDetailsIntent {
  data object Refresh : TaskDetailsIntent
  data object ToggleComplete : TaskDetailsIntent
  data class ToggleChecklistItem(val itemId: String) : TaskDetailsIntent
}
