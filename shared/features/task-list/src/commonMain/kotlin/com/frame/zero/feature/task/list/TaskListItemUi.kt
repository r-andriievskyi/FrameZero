package com.frame.zero.feature.task.list

import com.frame.zero.core.format.formatMedium
import com.frame.zero.domain.task.TaskStatus
import com.frame.zero.domain.task.TaskSummary

data class TaskListItemUi(
  val id: String,
  val title: String,
  val dueDateLabel: String?,
  val status: TaskStatus
)

fun TaskSummary.toUi(): TaskListItemUi =
  TaskListItemUi(
    id = id,
    title = title,
    dueDateLabel = dueDate?.formatMedium(),
    status = status
  )
