package com.frame.zero.dto.task

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
enum class TaskStatus {
  OPEN,
  DONE
}

@Serializable
enum class TaskPriority {
  HIGH,
  MEDIUM,
  LOW
}

@Serializable
data class TaskSummaryDto(
  val id: String,
  val title: String,
  val productionTitle: String,
  val dueDate: LocalDate?,
  val dueLabel: String?,
  val status: TaskStatus
)

@Serializable
data class TaskDetailDto(
  val id: String,
  val productionId: String,
  val productionTitle: String,
  val title: String,
  val description: String?,
  val dueDate: LocalDate?,
  val status: TaskStatus,
  val priority: TaskPriority,
  val assigneeUserId: String?,
  val assignee: TaskAssigneeDto?,
  val createdAt: Instant
)

@Serializable
data class TaskAssigneeDto(
  val userId: String,
  val name: String,
  val avatarColorHex: String?
)

@Serializable
data class CreateTaskRequest(
  val productionId: String,
  val title: String,
  val description: String? = null,
  val dueDate: LocalDate? = null,
  val assigneeUserId: String? = null
)

@Serializable
data class UpdateTaskRequest(
  val title: String? = null,
  val description: String? = null,
  val dueDate: LocalDate? = null,
  val status: TaskStatus? = null,
  val assigneeUserId: String? = null
)
