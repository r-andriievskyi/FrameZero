package com.frame.zero.feature.task.list

sealed interface TasksListIntent {
  data class FilterChanged(
    val filter: TaskListFilter
  ) : TasksListIntent

  data class SortChanged(
    val sort: TaskListSort
  ) : TasksListIntent
}
