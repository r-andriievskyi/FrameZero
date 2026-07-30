package com.frame.zero.feature.task.list

sealed interface TasksListIntent {
  data object Back : TasksListIntent

  data class TaskClick(val taskId: String) : TasksListIntent
}
