package com.frame.zero.feature.task.details

data class TaskDetailsState(
  val taskId: String = "",
  val title: String = "",
  val productionName: String = "",
  val priority: TaskPriority = TaskPriority.MEDIUM,
  val status: TaskStatus = TaskStatus.IN_PROGRESS,
  val assignee: TaskMember? = null,
  val reporter: TaskMember? = null,
  val dueDate: String? = null,
  val isDueToday: Boolean = false,
  val phase: String = "",
  val description: String = "",
  val tags: List<String> = emptyList(),
  val checklist: List<ChecklistItem> = emptyList(),
  val attachments: List<TaskAttachment> = emptyList(),
  val activity: List<ActivityEntry> = emptyList(),
  val isLoading: Boolean = false
)

enum class TaskPriority { HIGH, MEDIUM, LOW }

enum class TaskStatus { IN_PROGRESS, COMPLETED }

data class TaskMember(
  val initials: String,
  val name: String,
  val role: String,
  val avatarColorHex: String? = null
)

data class ChecklistItem(
  val id: String,
  val text: String,
  val isCompleted: Boolean
)

data class TaskAttachment(
  val id: String,
  val fileName: String,
  val fileType: String,
  val fileSize: String
)

data class ActivityEntry(
  val id: String,
  val initials: String,
  val avatarColorHex: String? = null,
  val text: String,
  val timestamp: String
)
