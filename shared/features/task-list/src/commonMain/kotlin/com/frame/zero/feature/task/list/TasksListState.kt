package com.frame.zero.feature.task.list

data class TasksListState(
  val filter: TaskListFilter = TaskListFilter.ALL,
  val sort: TaskListSort = TaskListSort.DUE_DATE
)
