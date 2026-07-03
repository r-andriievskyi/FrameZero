package com.frame.zero.task

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.common.Transactor
import com.frame.zero.common.parseUuidField
import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskAssigneeDto
import com.frame.zero.dto.task.TaskAttachmentDto
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.dto.task.TaskParticipantDto
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.dto.task.TaskSummaryDto
import com.frame.zero.dto.task.UpdateTaskParticipantsRequest
import com.frame.zero.dto.task.UpdateTaskRequest
import com.frame.zero.notification.NotificationRepository
import com.frame.zero.notification.TaskAssignmentNotifier
import com.frame.zero.production.AccessLevel
import com.frame.zero.production.ProductionAccessService
import com.frame.zero.production.ProductionMemberRepository
import com.frame.zero.storage.FileStorage
import java.util.UUID

class TaskService(
  private val tasks: TaskRepository,
  private val access: ProductionAccessService,
  private val members: ProductionMemberRepository,
  private val transactor: Transactor,
  private val notifications: NotificationRepository,
  private val assignmentNotifier: TaskAssignmentNotifier,
  private val fileStorage: FileStorage
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
    request: CreateTaskRequest,
    attachment: NewAttachment? = null,
    idempotencyKey: String? = null
  ): TaskDetailDto {
    if (idempotencyKey != null) {
      val existing = transactor.transaction { tasks.findByIdempotencyKey(idempotencyKey) }
      if (existing != null) {
        attachment?.let { fileStorage.delete(it.storageKey) }
        return existing.toDetailDto()
      }
    }

    var committed = false
    val (dto, assignment) = try {
      transactor.transaction {
        val title = request.title.trim()
        val description = request.description?.trim()
        validateCreate(title, description)

        val productionId = parseUuidField("productionId", request.productionId)

        access.requireAccess(userId, productionId, AccessLevel.WRITE)

        val assigneeId = request.assigneeUserId?.let { parseUuidField("assigneeUserId", it) }
        val participantIds = validatedParticipantIds(productionId, request.participantUserIds)

        val task = tasks.create(
          productionId = productionId,
          title = title,
          description = description,
          dueDate = request.dueDate,
          assigneeUserId = assigneeId,
          priority = request.priority,
          idempotencyKey = idempotencyKey,
          attachment = attachment,
          createdByUserId = userId,
          participantUserIds = participantIds
        )

        val notifyAssignee = assigneeId?.takeIf { it != userId }
        if (notifyAssignee != null) {
          notifications.create(
            userId = notifyAssignee,
            body = task.title
          )
        }
        task.toDetailDto() to notifyAssignee?.let { it to task.id }
      }.also { committed = true }
    } catch (_: DuplicateIdempotencyKeyException) {
      // A concurrent retry with the same Idempotency-Key won the insert race; our
      // transaction rolled back. Return the winner's task. The blob we stored (if
      // any) is now orphaned and is removed by the finally below.
      val key = requireNotNull(idempotencyKey) { "Duplicate idempotency collision without a key" }
      val winner = transactor.transaction { tasks.findByIdempotencyKey(key) }
        ?: throw AppException(AppError.Conflict("Duplicate idempotency key"))
      return winner.toDetailDto()
    } finally {
      if (!committed) attachment?.let { fileStorage.delete(it.storageKey) }
    }

    assignment?.let { (assigneeId, taskId) ->
      assignmentNotifier.notifyTaskAssigned(assigneeId, taskId, dto.title)
    }
    return dto
  }

  private fun validateCreate(
    title: String,
    description: String?
  ) {
    val errors = mutableMapOf<String, String>()
    if (title.isBlank()) errors["title"] = "Required"
    if (title.length > MAX_TITLE_LENGTH) errors["title"] = "Max $MAX_TITLE_LENGTH characters"
    if ((description?.length ?: 0) > MAX_DESCRIPTION_LENGTH) {
      errors["description"] = "Max $MAX_DESCRIPTION_LENGTH characters"
    }
    if (errors.isNotEmpty()) throw AppException(AppError.ValidationError(errors))
  }

  suspend fun getAttachment(
    userId: UUID,
    taskId: UUID
  ): AttachmentRecord {
    val attachment = transactor.transaction {
      val task = tasks.findById(taskId) ?: throw AppException(AppError.NotFound)
      access.requireAccess(userId, task.productionId, AccessLevel.READ)
      task.attachment
    }
    if (attachment == null || !fileStorage.exists(attachment.storageKey)) {
      throw AppException(AppError.NotFound)
    }
    return attachment
  }

  suspend fun update(
    userId: UUID,
    taskId: UUID,
    request: UpdateTaskRequest
  ): TaskDetailDto {
    val (dto, assignment) = transactor.transaction {
      val task = tasks.findById(taskId) ?: throw AppException(AppError.NotFound)
      access.requireAccess(userId, task.productionId, AccessLevel.WRITE)

      validateUpdate(request)

      val assigneeId = request.assigneeUserId?.let { parseUuidField("assigneeUserId", it) }

      val participantIds = request.participantUserIds?.let { rawIds ->
        requireParticipantEditor(userId, task)
        validatedParticipantIds(task.productionId, rawIds)
      }

      val updated = tasks.update(
        id = taskId,
        title = request.title?.trim(),
        description = request.description?.trim(),
        dueDate = request.dueDate,
        status = request.status,
        assigneeUserId = assigneeId,
        participantUserIds = participantIds
      ) ?: throw AppException(AppError.NotFound)

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

  suspend fun updateParticipants(
    userId: UUID,
    taskId: UUID,
    request: UpdateTaskParticipantsRequest
  ): TaskDetailDto =
    transactor.transaction {
      val task = tasks.findById(taskId) ?: throw AppException(AppError.NotFound)
      // Any production member may read the task; membership stays the outer
      // boundary, with the creator/assignee rule layered on top for writes.
      access.requireAccess(userId, task.productionId, AccessLevel.WRITE)
      requireParticipantEditor(userId, task)

      val participantIds = validatedParticipantIds(task.productionId, request.participantUserIds)
      val updated = tasks.update(
        id = taskId,
        title = null,
        description = null,
        dueDate = null,
        status = null,
        assigneeUserId = null,
        participantUserIds = participantIds
      ) ?: throw AppException(AppError.NotFound)
      updated.toDetailDto()
    }

  /** Only the task creator or the current assignee may modify the participant list. */
  private fun requireParticipantEditor(
    userId: UUID,
    task: TaskRecord
  ) {
    val isCreator = task.createdByUserId != null && task.createdByUserId == userId
    val isAssignee = task.assigneeUserId != null && task.assigneeUserId == userId
    if (!isCreator && !isAssignee) throw AppException(AppError.Forbidden)
  }

  /**
   * Parses and dedupes the raw participant ids, then checks each one is a
   * member of the task's production (one membership query, not one per id).
   */
  private suspend fun validatedParticipantIds(
    productionId: UUID,
    rawParticipantUserIds: List<String>
  ): Set<UUID> {
    val participantIds = rawParticipantUserIds
      .map { parseUuidField("participantUserIds", it) }
      .toSet()
    if (participantIds.isEmpty()) return participantIds

    val memberUserIds = members.findByProduction(productionId).mapNotNullTo(mutableSetOf()) { it.userId }
    val outsiders = participantIds - memberUserIds
    if (outsiders.isNotEmpty()) {
      throw AppException(
        AppError.ValidationError(
          mapOf("participantUserIds" to "Not a member of the production: ${outsiders.joinToString()}")
        )
      )
    }
    return participantIds
  }

  suspend fun delete(
    userId: UUID,
    taskId: UUID
  ) {
    val storageKey = transactor.transaction {
      val task = tasks.findById(taskId) ?: throw AppException(AppError.NotFound)
      access.requireAccess(userId, task.productionId, AccessLevel.WRITE)
      val key = task.attachment?.storageKey
      tasks.delete(taskId)
      key
    }
    storageKey?.let { fileStorage.delete(it) }
  }

  private fun validateUpdate(request: UpdateTaskRequest) {
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
      createdAt = createdAt,
      attachment = attachment?.let {
        TaskAttachmentDto(
          fileName = it.fileName,
          sizeBytes = it.sizeBytes,
          contentType = it.contentType
        )
      },
      participants = participants.map {
        TaskParticipantDto(
          userId = it.userId.toString(),
          name = it.name,
          avatarColorHex = it.avatarColorHex
        )
      }
    )

  private companion object {
    // Title matches the TasksTable column size; description is an unbounded
    // text column, so the cap is a request-sanity bound rather than a schema one.
    const val MAX_TITLE_LENGTH = 200
    const val MAX_DESCRIPTION_LENGTH = 10_000
  }
}
