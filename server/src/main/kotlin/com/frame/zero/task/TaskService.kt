package com.frame.zero.task

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.common.Transactor
import com.frame.zero.common.parseUuidField
import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskAssigneeDto
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.dto.task.TaskSummaryDto
import com.frame.zero.dto.task.UpdateTaskRequest
import com.frame.zero.notification.NotificationRepository
import com.frame.zero.notification.TaskAssignmentNotifier
import com.frame.zero.production.AccessLevel
import com.frame.zero.production.ProductionAccessService
import java.util.UUID

class TaskService(
  private val tasks: TaskRepository,
  private val access: ProductionAccessService,
  private val transactor: Transactor,
  private val notifications: NotificationRepository,
  private val assignmentNotifier: TaskAssignmentNotifier
) {
  suspend fun list(
    userId: UUID,
    assigneeMe: Boolean,
    status: TaskStatus?,
    productionId: UUID?,
    limit: Int,
    cursor: String?
  ): Pair<List<TaskSummaryDto>, String?> =
    transactor.transaction {
      if (productionId != null) access.requireAccess(userId, productionId, AccessLevel.READ)
      val (items, nextCursor) = tasks.findForUser(
        userId = userId,
        assigneeMe = assigneeMe,
        status = status,
        productionId = productionId,
        limit = limit,
        cursor = cursor
      )
      Pair(items.map { it.toSummaryDto() }, nextCursor)
    }

  suspend fun get(
    userId: UUID,
    taskId: UUID
  ): TaskDetailDto =
    transactor.transaction {
      val task = tasks.findById(taskId) ?: throw AppException(AppError.NotFound)
      access.requireAccess(userId, task.productionId, AccessLevel.READ)
      task.toDetailDto()
    }

  suspend fun create(
    userId: UUID,
    request: CreateTaskRequest
  ): TaskDetailDto {
    val (dto, assignment) =
      transactor.transaction {
        // Validate the trimmed values that will actually be persisted, so a title
        // that only exceeds the cap because of surrounding whitespace isn't rejected.
        val title = request.title.trim()
        val description = request.description?.trim()
        val errors = mutableMapOf<String, String>()
        if (title.isBlank()) errors["title"] = "Required"
        if (title.length > MAX_TITLE_LENGTH) {
          errors["title"] = "Max $MAX_TITLE_LENGTH characters"
        }
        if ((description?.length ?: 0) > MAX_DESCRIPTION_LENGTH) {
          errors["description"] = "Max $MAX_DESCRIPTION_LENGTH characters"
        }
        if (errors.isNotEmpty()) throw AppException(AppError.ValidationError(errors))

        val productionId = parseUuidField("productionId", request.productionId)

        access.requireAccess(userId, productionId, AccessLevel.WRITE)

        val assigneeId = request.assigneeUserId?.let { parseUuidField("assigneeUserId", it) }

        val task = tasks.create(
          productionId = productionId,
          title = title,
          description = description,
          dueDate = request.dueDate,
          assigneeUserId = assigneeId,
          priority = request.priority
        )

        // Notify the assignee unless they assigned the task to themselves. The in-app
        // record is written inside the transaction (atomic with the task); the push is
        // fired after commit so network I/O stays out of the DB transaction.
        val notifyAssignee = assigneeId?.takeIf { it != userId }
        if (notifyAssignee != null) {
          notifications.create(
            userId = notifyAssignee,
            body = task.title
          )
        }
        task.toDetailDto() to notifyAssignee?.let { it to task.id }
      }

    assignment?.let { (assigneeId, taskId) ->
      assignmentNotifier.notifyTaskAssigned(assigneeId, taskId, dto.title)
    }
    return dto
  }

  suspend fun update(
    userId: UUID,
    taskId: UUID,
    request: UpdateTaskRequest
  ): TaskDetailDto {
    val (dto, assignment) =
      transactor.transaction {
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

        val assigneeId = request.assigneeUserId?.let { parseUuidField("assigneeUserId", it) }

        val updated =
          tasks.update(
            id = taskId,
            title = request.title?.trim(),
            description = request.description?.trim(),
            dueDate = request.dueDate,
            status = request.status,
            assigneeUserId = assigneeId
          ) ?: throw AppException(AppError.NotFound)

        // Notify on a reassignment to a *different* user (a null assigneeId means
        // "leave the assignee unchanged"), skipping self-assignment — mirrors create().
        // In-app record is atomic with the update; the push fires after commit.
        val notifyAssignee = assigneeId?.takeIf { it != task.assigneeUserId && it != userId }
        if (notifyAssignee != null) {
          notifications.create(
            userId = notifyAssignee,
            body = updated.title
          )
        }
        updated.toDetailDto() to notifyAssignee?.let { it to updated.id }
      }

    assignment?.let { (assigneeId, id) ->
      assignmentNotifier.notifyTaskAssigned(assigneeId, id, dto.title)
    }
    return dto
  }

  suspend fun delete(
    userId: UUID,
    taskId: UUID
  ): Unit =
    transactor.transaction {
      val task = tasks.findById(taskId) ?: throw AppException(AppError.NotFound)
      access.requireAccess(userId, task.productionId, AccessLevel.WRITE)
      tasks.delete(taskId)
    }

  private fun TaskRecord.toSummaryDto(): TaskSummaryDto =
    TaskSummaryDto(
      id = id.toString(),
      title = title,
      productionTitle = productionTitle,
      dueDate = dueDate,
      status = status
    )

  private fun TaskRecord.toDetailDto(): TaskDetailDto =
    TaskDetailDto(
      id = id.toString(),
      productionId = productionId.toString(),
      productionTitle = productionTitle,
      title = title,
      description = description,
      dueDate = dueDate,
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
      createdAt = createdAt
    )

  private companion object {
    // Title matches the TasksTable column size; description is an unbounded
    // text column, so the cap is a request-sanity bound rather than a schema one.
    const val MAX_TITLE_LENGTH = 200
    const val MAX_DESCRIPTION_LENGTH = 10_000
  }
}
