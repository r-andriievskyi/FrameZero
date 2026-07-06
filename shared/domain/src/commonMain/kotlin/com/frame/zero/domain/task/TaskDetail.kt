package com.frame.zero.domain.task

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class TaskDetail(
  val id: String,
  val productionId: String,
  val productionTitle: String,
  val title: String,
  val description: String?,
  val dueDate: LocalDate?,
  val status: TaskStatus,
  val priority: TaskPriority,
  val assigneeUserId: String?,
  val assignee: TaskAssignee?,
  val createdAt: Instant,
  val attachment: TaskAttachment? = null,
  val participants: List<TaskParticipant> = emptyList()
)

data class TaskAssignee(
  val userId: String,
  val name: String,
  val avatarColorHex: String?
)

data class TaskParticipant(
  val userId: String,
  val name: String,
  val avatarColorHex: String?
)

data class TaskAttachment(
  val fileName: String,
  val sizeBytes: Long,
  val contentType: String
)
