package com.frame.zero.domain.task

import kotlinx.datetime.LocalDate

/** Everything needed to create a task; the repository maps it to the wire request. */
data class NewTask(
  val productionId: String,
  val title: String,
  val description: String? = null,
  val dueDate: LocalDate? = null,
  val assigneeUserId: String? = null,
  val priority: TaskPriority = TaskPriority.MEDIUM,
  val participantUserIds: List<String> = emptyList()
)
