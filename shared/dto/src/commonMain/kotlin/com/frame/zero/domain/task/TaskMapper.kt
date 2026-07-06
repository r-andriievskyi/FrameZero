package com.frame.zero.domain.task

import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskAssigneeDto
import com.frame.zero.dto.task.TaskAttachmentDto
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.dto.task.TaskParticipantDto
import com.frame.zero.dto.task.TaskSummaryDto

fun TaskDetailDto.toDomain(): TaskDetail =
  TaskDetail(
    id = id,
    productionId = productionId,
    productionTitle = productionTitle,
    title = title,
    description = description,
    dueDate = dueDate,
    status = status,
    priority = priority,
    assigneeUserId = assigneeUserId,
    assignee = assignee?.toDomain(),
    createdAt = createdAt,
    attachment = attachment?.toDomain(),
    participants = participants.map { it.toDomain() }
  )

fun TaskAssigneeDto.toDomain(): TaskAssignee =
  TaskAssignee(userId = userId, name = name, avatarColorHex = avatarColorHex)

fun TaskParticipantDto.toDomain(): TaskParticipant =
  TaskParticipant(userId = userId, name = name, avatarColorHex = avatarColorHex)

fun TaskAttachmentDto.toDomain(): TaskAttachment =
  TaskAttachment(fileName = fileName, sizeBytes = sizeBytes, contentType = contentType)

fun TaskSummaryDto.toDomain(): TaskSummary =
  TaskSummary(
    id = id,
    title = title,
    productionTitle = productionTitle,
    dueDate = dueDate,
    status = status
  )

fun NewTask.toCreateRequest(): CreateTaskRequest =
  CreateTaskRequest(
    productionId = productionId,
    title = title,
    description = description,
    dueDate = dueDate,
    assigneeUserId = assigneeUserId,
    priority = priority,
    participantUserIds = participantUserIds
  )
