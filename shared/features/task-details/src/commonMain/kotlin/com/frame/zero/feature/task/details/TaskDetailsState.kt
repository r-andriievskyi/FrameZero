package com.frame.zero.feature.task.details

data class TaskDetailsState(
  val taskId: String = "",
  val title: String = "",
  val productionName: String = "",
  val priority: TaskPriority = TaskPriority.MEDIUM,
  val status: TaskStatus = TaskStatus.IN_PROGRESS,
  val assignee: TaskMember? = null,
  val dueDate: String? = null,
  val isDueToday: Boolean = false,
  val description: String = "",
  val isLoading: Boolean = false,
  val isError: Boolean = false,
  val showMarkCompleteButton: Boolean = false
)

enum class TaskPriority { HIGH, MEDIUM, LOW }

enum class TaskStatus { IN_PROGRESS, COMPLETED }

data class TaskMember(
  val initials: String,
  val name: String,
  val avatarColorHex: String? = null
)
