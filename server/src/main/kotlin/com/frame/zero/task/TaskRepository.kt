package com.frame.zero.task

import com.frame.zero.auth.UsersTable
import com.frame.zero.common.decodeCursor
import com.frame.zero.common.encodeCursor
import com.frame.zero.common.nowTruncatedToMicros
import com.frame.zero.config.dbQuery
import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.production.ProductionMembersTable
import com.frame.zero.production.ProductionsTable
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.sql.SQLException
import java.util.UUID
import kotlin.time.Instant

private const val NULL_DUE_DATE_CURSOR = Long.MAX_VALUE
private const val UNIQUE_VIOLATION_SQL_STATE = "23505"

/**
 * Thrown when a task insert collides with an existing row's unique idempotency
 * key — i.e. a concurrent retry with the same `Idempotency-Key` won the race.
 * The caller should return the already-persisted task instead of a 500.
 */
class DuplicateIdempotencyKeyException : RuntimeException()

data class TaskRecord(
  val id: UUID,
  val productionId: UUID,
  val productionTitle: String,
  val title: String,
  val description: String?,
  val dueDate: LocalDate?,
  val status: TaskStatus,
  val priority: TaskPriority,
  val assigneeUserId: UUID?,
  val assigneeName: String?,
  val assigneeAvatarColorHex: String?,
  val createdAt: Instant,
  val attachment: AttachmentRecord? = null
)

/** Attachment metadata for a task; [storageKey] locates the bytes in [com.frame.zero.storage.FileStorage]. */
data class AttachmentRecord(
  val fileName: String,
  val contentType: String,
  val sizeBytes: Long,
  val storageKey: String
)

/** A freshly stored blob ready to be linked to a new task. */
data class NewAttachment(
  val fileName: String,
  val contentType: String,
  val sizeBytes: Long,
  val storageKey: String
)

interface TaskRepository {
  suspend fun create(
    productionId: UUID,
    title: String,
    description: String?,
    dueDate: LocalDate?,
    assigneeUserId: UUID?,
    priority: TaskPriority = TaskPriority.MEDIUM,
    idempotencyKey: String? = null,
    attachment: NewAttachment? = null
  ): TaskRecord

  suspend fun findById(id: UUID): TaskRecord?

  suspend fun findByIdempotencyKey(idempotencyKey: String): TaskRecord?

  suspend fun findAttachment(taskId: UUID): AttachmentRecord?

  suspend fun findForUser(
    userId: UUID,
    assigneeMe: Boolean,
    status: TaskStatus?,
    productionId: UUID?,
    limit: Int,
    cursor: String?
  ): Pair<List<TaskRecord>, String?>

  suspend fun findForUserLimit(
    userId: UUID,
    limit: Int
  ): List<TaskRecord>

  suspend fun findInRangeForUser(
    userId: UUID,
    rangeStart: LocalDate,
    rangeEnd: LocalDate
  ): List<TaskRecord>

  suspend fun countOpenForUser(userId: UUID): Int

  suspend fun update(
    id: UUID,
    title: String?,
    description: String?,
    dueDate: LocalDate?,
    status: TaskStatus?,
    assigneeUserId: UUID?
  ): TaskRecord?

  suspend fun delete(id: UUID): Boolean
}

class TaskRepositoryImpl : TaskRepository {
  override suspend fun create(
    productionId: UUID,
    title: String,
    description: String?,
    dueDate: LocalDate?,
    assigneeUserId: UUID?,
    priority: TaskPriority,
    idempotencyKey: String?,
    attachment: NewAttachment?
  ): TaskRecord =
    dbQuery {
      val newId = UUID.randomUUID()
      val now = nowTruncatedToMicros()
      try {
        TasksTable.insert {
          it[id] = newId
          it[TasksTable.productionId] = productionId
          it[TasksTable.title] = title
          it[TasksTable.description] = description
          it[TasksTable.dueDate] = dueDate
          it[status] = TaskStatus.OPEN.name
          it[TasksTable.priority] = priority.name
          it[TasksTable.assigneeUserId] = assigneeUserId
          it[createdAt] = now
          it[TasksTable.idempotencyKey] = idempotencyKey
        }
      } catch (exception: SQLException) {
        // Two concurrent requests with the same Idempotency-Key can both pass the
        // service-level findByIdempotencyKey check; the loser collides with the
        // unique index here and must surface so the caller can return the winner.
        if (idempotencyKey != null && exception.isUniqueViolation()) {
          throw DuplicateIdempotencyKeyException()
        }
        throw exception
      }
      if (attachment != null) {
        TaskAttachmentsTable.insert {
          it[id] = UUID.randomUUID()
          it[taskId] = newId
          it[fileName] = attachment.fileName
          it[contentType] = attachment.contentType
          it[sizeBytes] = attachment.sizeBytes
          it[storageKey] = attachment.storageKey
          it[createdAt] = now
        }
      }
      val prodTitle = ProductionsTable
        .selectAll()
        .where { ProductionsTable.id eq productionId }
        .singleOrNull()
        ?.get(ProductionsTable.title).orEmpty()
      TaskRecord(
        id = newId,
        productionId = productionId,
        productionTitle = prodTitle,
        title = title,
        description = description,
        dueDate = dueDate,
        status = TaskStatus.OPEN,
        priority = priority,
        assigneeUserId = assigneeUserId,
        assigneeName = null,
        assigneeAvatarColorHex = null,
        createdAt = now,
        attachment = attachment?.let {
          AttachmentRecord(it.fileName, it.contentType, it.sizeBytes, it.storageKey)
        }
      )
    }

  override suspend fun findById(id: UUID): TaskRecord? =
    dbQuery {
      tasksWithRelations
        .selectAll()
        .where { TasksTable.id eq id }
        .singleOrNull()
        ?.toRecord()
        ?.let { it.copy(attachment = attachmentFor(it.id)) }
    }

  override suspend fun findByIdempotencyKey(idempotencyKey: String): TaskRecord? =
    dbQuery {
      tasksWithRelations
        .selectAll()
        .where { TasksTable.idempotencyKey eq idempotencyKey }
        .singleOrNull()
        ?.toRecord()
        ?.let { it.copy(attachment = attachmentFor(it.id)) }
    }

  override suspend fun findAttachment(taskId: UUID): AttachmentRecord? = dbQuery { attachmentFor(taskId) }

  override suspend fun findForUser(
    userId: UUID,
    assigneeMe: Boolean,
    status: TaskStatus?,
    productionId: UUID?,
    limit: Int,
    cursor: String?
  ): Pair<List<TaskRecord>, String?> =
    dbQuery {
      val accessibleProductionIds = accessibleProductionIds(userId)
      if (accessibleProductionIds.isEmpty()) return@dbQuery Pair(emptyList(), null)

      val rows = tasksWithRelations
        .selectAll()
        .where {
          // Always scope to productions the caller owns or is a member of; the
          // optional filters below only narrow within that set, never widen it.
          var cond: Op<Boolean> = TasksTable.productionId inList accessibleProductionIds
          if (assigneeMe) cond = cond and (TasksTable.assigneeUserId eq userId)
          if (status != null) cond = cond and (TasksTable.status eq status.name)
          if (productionId != null) cond = cond and (TasksTable.productionId eq productionId)
          if (cursor != null) {
            val pc = decodeCursor(cursor)
            if (pc != null) {
              // Keyset predicate must match the (dueDate ASC NULLS LAST, id ASC)
              // sort below. Null due dates sort last and are encoded as the
              // NULL_DUE_DATE_CURSOR sentinel.
              cond = cond and
                if (pc.sortKey == NULL_DUE_DATE_CURSOR) {
                  TasksTable.dueDate.isNull() and (TasksTable.id greater pc.id)
                } else {
                  val cursorDue = LocalDate.fromEpochDays(pc.sortKey)
                  (TasksTable.dueDate greater cursorDue) or
                    ((TasksTable.dueDate eq cursorDue) and (TasksTable.id greater pc.id)) or
                    TasksTable.dueDate.isNull()
                }
            }
          }
          cond
        }.orderBy(TasksTable.dueDate to SortOrder.ASC_NULLS_LAST, TasksTable.id to SortOrder.ASC)
        .limit(limit + 1)
        .map { it.toRecord() }

      val hasMore = rows.size > limit
      val items = if (hasMore) rows.dropLast(1) else rows
      val nextCursor =
        if (hasMore) {
          val last = items.last()
          encodeCursor(last.dueDate?.toEpochDays() ?: NULL_DUE_DATE_CURSOR, last.id)
        } else {
          null
        }
      Pair(items, nextCursor)
    }

  override suspend fun findForUserLimit(
    userId: UUID,
    limit: Int
  ): List<TaskRecord> =
    dbQuery {
      tasksWithRelations
        .selectAll()
        .where {
          (TasksTable.assigneeUserId eq userId) and
            (TasksTable.status eq TaskStatus.OPEN.name) and
            ProductionsTable.deletedAt.isNull()
        }.orderBy(TasksTable.dueDate to SortOrder.ASC_NULLS_LAST)
        .limit(limit)
        .map { it.toRecord() }
    }

  override suspend fun findInRangeForUser(
    userId: UUID,
    rangeStart: LocalDate,
    rangeEnd: LocalDate
  ): List<TaskRecord> =
    dbQuery {
      val memberProductionIds = ProductionMembersTable
        .selectAll()
        .where { ProductionMembersTable.userId eq userId }
        .map { it[ProductionMembersTable.productionId] }
      if (memberProductionIds.isEmpty()) return@dbQuery emptyList()

      tasksWithRelations
        .selectAll()
        .where {
          TasksTable.dueDate.isNotNull() and
            (TasksTable.dueDate greaterEq rangeStart) and
            (TasksTable.dueDate lessEq rangeEnd) and
            (TasksTable.productionId inList memberProductionIds) and
            ProductionsTable.deletedAt.isNull()
        }.orderBy(TasksTable.dueDate to SortOrder.ASC, TasksTable.id to SortOrder.ASC)
        .map { it.toRecord() }
    }

  override suspend fun countOpenForUser(userId: UUID): Int =
    dbQuery {
      (TasksTable innerJoin ProductionsTable)
        .selectAll()
        .where {
          (TasksTable.assigneeUserId eq userId) and
            (TasksTable.status eq TaskStatus.OPEN.name) and
            ProductionsTable.deletedAt.isNull()
        }.count()
        .toInt()
    }

  override suspend fun update(
    id: UUID,
    title: String?,
    description: String?,
    dueDate: LocalDate?,
    status: TaskStatus?,
    assigneeUserId: UUID?
  ): TaskRecord? =
    dbQuery {
      val updated = TasksTable.update({ TasksTable.id eq id }) { row ->
        title?.let { row[TasksTable.title] = it }
        description?.let { row[TasksTable.description] = it }
        dueDate?.let { row[TasksTable.dueDate] = it }
        status?.let { row[TasksTable.status] = it.name }
        assigneeUserId?.let { row[TasksTable.assigneeUserId] = it }
      }
      if (updated == 0) {
        null
      } else {
        tasksWithRelations
          .selectAll()
          .where { TasksTable.id eq id }
          .singleOrNull()
          ?.toRecord()
      }
    }

  override suspend fun delete(id: UUID): Boolean =
    dbQuery {
      TasksTable.deleteWhere { TasksTable.id eq id } > 0
    }

  private fun SQLException.isUniqueViolation(): Boolean =
    generateSequence(this as Throwable) { it.cause }
      .filterIsInstance<SQLException>()
      .any { it.sqlState == UNIQUE_VIOLATION_SQL_STATE }

  private fun accessibleProductionIds(userId: UUID): List<UUID> {
    val memberProductionIds = ProductionMembersTable
      .selectAll()
      .where { ProductionMembersTable.userId eq userId }
      .map { it[ProductionMembersTable.productionId] }
    return ProductionsTable
      .selectAll()
      .where {
        val accessCond = if (memberProductionIds.isEmpty()) {
          ProductionsTable.ownerUserId eq userId
        } else {
          (ProductionsTable.ownerUserId eq userId) or
            (ProductionsTable.id inList memberProductionIds)
        }
        ProductionsTable.deletedAt.isNull() and accessCond
      }.map { it[ProductionsTable.id] }
  }

  // Tasks joined with their production (always present) and assignee user
  // (optional, hence a left join so unassigned tasks still return).
  private val tasksWithRelations
    get() = (TasksTable innerJoin ProductionsTable).join(
      UsersTable,
      JoinType.LEFT,
      TasksTable.assigneeUserId,
      UsersTable.id
    )

  // Attachment metadata for a task, if any. Must be called inside a dbQuery
  // transaction. Kept separate from the list join so summary queries stay lean.
  private fun attachmentFor(taskId: UUID): AttachmentRecord? =
    TaskAttachmentsTable
      .selectAll()
      .where { TaskAttachmentsTable.taskId eq taskId }
      .singleOrNull()
      ?.let {
        AttachmentRecord(
          fileName = it[TaskAttachmentsTable.fileName],
          contentType = it[TaskAttachmentsTable.contentType],
          sizeBytes = it[TaskAttachmentsTable.sizeBytes],
          storageKey = it[TaskAttachmentsTable.storageKey]
        )
      }

  private fun ResultRow.toRecord(): TaskRecord =
    TaskRecord(
      id = this[TasksTable.id],
      productionId = this[TasksTable.productionId],
      productionTitle = this[ProductionsTable.title],
      title = this[TasksTable.title],
      description = this[TasksTable.description],
      dueDate = this[TasksTable.dueDate],
      status = TaskStatus.valueOf(this[TasksTable.status]),
      priority = runCatching { TaskPriority.valueOf(this[TasksTable.priority]) }
        .getOrDefault(TaskPriority.MEDIUM),
      assigneeUserId = this[TasksTable.assigneeUserId],
      assigneeName = this.getOrNull(UsersTable.firstName)?.let {
        "$it ${this.getOrNull(UsersTable.lastName).orEmpty()}".trim()
      },
      assigneeAvatarColorHex = this.getOrNull(UsersTable.avatarColorHex),
      createdAt = this[TasksTable.createdAt]
    )
}
