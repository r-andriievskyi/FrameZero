package com.frame.zero.task

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.common.toJava
import com.frame.zero.common.toKotlin
import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskAssigneeDto
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.dto.task.TaskSummaryDto
import com.frame.zero.dto.task.UpdateTaskRequest
import com.frame.zero.production.AccessLevel
import com.frame.zero.production.ProductionAccessService
import java.time.ZoneId
import java.util.UUID
import kotlin.time.toKotlinInstant

class TaskService(
  private val tasks: TaskRepository,
  private val access: ProductionAccessService
) {
  suspend fun list(
    userId: UUID,
    assigneeMe: Boolean,
    status: TaskStatus?,
    productionId: UUID?,
    limit: Int,
    cursor: String?,
    timezone: ZoneId
  ): Pair<List<TaskSummaryDto>, String?> {
    if (productionId != null) access.requireAccess(userId, productionId, AccessLevel.READ)
    val (items, nextCursor) =
      tasks.findForUser(
        userId = userId,
        assigneeMe = assigneeMe,
        status = status,
        productionId = productionId,
        limit = limit,
        cursor = cursor
      )
    return Pair(items.map { it.toSummaryDto() }, nextCursor)
  }

  suspend fun get(
    userId: UUID,
    taskId: UUID,
    timezone: ZoneId
  ): TaskDetailDto {
    val task = tasks.findById(taskId) ?: throw AppException(AppError.NotFound)
    access.requireAccess(userId, task.productionId, AccessLevel.READ)
    return task.toDetailDto()
  }

  suspend fun create(
    userId: UUID,
    request: CreateTaskRequest,
    timezone: ZoneId
  ): TaskDetailDto {
    val errors = mutableMapOf<String, String>()
    if (request.title.isBlank()) errors["title"] = "Required"
    if (request.title.length > MAX_TITLE_LENGTH) {
      errors["title"] = "Max $MAX_TITLE_LENGTH characters"
    }
    if ((request.description?.length ?: 0) > MAX_DESCRIPTION_LENGTH) {
      errors["description"] = "Max $MAX_DESCRIPTION_LENGTH characters"
    }
    if (errors.isNotEmpty()) throw AppException(AppError.ValidationError(errors))

    val productionId =
      runCatching { UUID.fromString(request.productionId) }.getOrNull()
        ?: throw AppException(AppError.ValidationError(mapOf("productionId" to "Invalid UUID")))

    access.requireAccess(userId, productionId, AccessLevel.WRITE)

    val assigneeId =
      request.assigneeUserId?.let {
        runCatching { UUID.fromString(it) }.getOrNull()
          ?: throw AppException(AppError.ValidationError(mapOf("assigneeUserId" to "Invalid UUID")))
      }

    val task =
      tasks.create(
        productionId = productionId,
        title = request.title.trim(),
        description = request.description?.trim(),
        dueDate = request.dueDate?.toJava(),
        assigneeUserId = assigneeId
      )
    return task.toDetailDto()
  }

  suspend fun update(
    userId: UUID,
    taskId: UUID,
    request: UpdateTaskRequest,
    timezone: ZoneId
  ): TaskDetailDto {
    val task = tasks.findById(taskId) ?: throw AppException(AppError.NotFound)
    access.requireAccess(userId, task.productionId, AccessLevel.WRITE)

    val errors = mutableMapOf<String, String>()
    val requestTitle = request.title
    if (requestTitle != null && requestTitle.isBlank()) errors["title"] = "Cannot be empty"
    if (requestTitle != null && requestTitle.length > MAX_TITLE_LENGTH) {
      errors["title"] = "Max $MAX_TITLE_LENGTH characters"
    }
    if ((request.description?.length ?: 0) > MAX_DESCRIPTION_LENGTH) {
      errors["description"] = "Max $MAX_DESCRIPTION_LENGTH characters"
    }
    if (errors.isNotEmpty()) throw AppException(AppError.ValidationError(errors))

    val assigneeId =
      request.assigneeUserId?.let {
        runCatching { UUID.fromString(it) }.getOrNull()
          ?: throw AppException(AppError.ValidationError(mapOf("assigneeUserId" to "Invalid UUID")))
      }

    val updated =
      tasks.update(
        id = taskId,
        title = request.title?.trim(),
        description = request.description?.trim(),
        dueDate = request.dueDate?.toJava(),
        status = request.status,
        assigneeUserId = assigneeId
      ) ?: throw AppException(AppError.NotFound)
    return updated.toDetailDto()
  }

  suspend fun delete(
    userId: UUID,
    taskId: UUID
  ) {
    val task = tasks.findById(taskId) ?: throw AppException(AppError.NotFound)
    access.requireAccess(userId, task.productionId, AccessLevel.WRITE)
    tasks.delete(taskId)
  }

  private fun TaskRecord.toSummaryDto(): TaskSummaryDto =
    TaskSummaryDto(
      id = id.toString(),
      title = title,
      productionTitle = productionTitle,
      dueDate = dueDate?.toKotlin(),
      status = status
    )

  private fun TaskRecord.toDetailDto(): TaskDetailDto =
    TaskDetailDto(
      id = id.toString(),
      productionId = productionId.toString(),
      productionTitle = productionTitle,
      title = title,
      description = description,
      dueDate = dueDate?.toKotlin(),
      status = status,
      priority = priority,
      assigneeUserId = assigneeUserId?.toString(),
      assignee = assigneeUserId?.let {
        TaskAssigneeDto(
          userId = it.toString(),
          name = assigneeName.orEmpty(),
          avatarColorHex = assigneeAvatarColorHex
        )
      },
      createdAt = createdAt.toKotlinInstant()
    )

  private companion object {
    // Title matches the TasksTable column size; description is an unbounded
    // text column, so the cap is a request-sanity bound rather than a schema one.
    const val MAX_TITLE_LENGTH = 200
    const val MAX_DESCRIPTION_LENGTH = 10_000
  }
}
