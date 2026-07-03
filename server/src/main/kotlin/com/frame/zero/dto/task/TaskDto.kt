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
  val createdAt: Instant,
  val attachment: TaskAttachmentDto? = null,
  val participants: List<TaskParticipantDto> = emptyList()
)

@Serializable
data class TaskAssigneeDto(
  val userId: String,
  val name: String,
  val avatarColorHex: String?
)

@Serializable
data class TaskParticipantDto(
  val userId: String,
  val name: String,
  val avatarColorHex: String?
)

@Serializable
data class TaskAttachmentDto(
  val fileName: String,
  val sizeBytes: Long,
  val contentType: String
)

@Serializable
data class CreateTaskRequest(
  val productionId: String,
  val title: String,
  val description: String? = null,
  val dueDate: LocalDate? = null,
  val assigneeUserId: String? = null,
  val priority: TaskPriority = TaskPriority.MEDIUM,
  val participantUserIds: List<String> = emptyList()
)

@Serializable
data class UpdateTaskRequest(
  val title: String? = null,
  val description: String? = null,
  val dueDate: LocalDate? = null,
  val status: TaskStatus? = null,
  val assigneeUserId: String? = null,
  // Null means "leave participants unchanged"; a list (even empty) replaces them.
  val participantUserIds: List<String>? = null
)

@Serializable
data class UpdateTaskParticipantsRequest(
  val participantUserIds: List<String>
)
