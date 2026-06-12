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
import java.util.UUID
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

// Cursor sentinel for tasks without a due date; they sort after every real
// date (NULLS LAST), so the sentinel must be the maximum encodable value.
private const val NULL_DUE_DATE_CURSOR = Long.MAX_VALUE

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
  val createdAt: Instant
)

interface TaskRepository {
  suspend fun create(
    productionId: UUID,
    title: String,
    description: String?,
    dueDate: LocalDate?,
    assigneeUserId: UUID?
  ): TaskRecord

  suspend fun findById(id: UUID): TaskRecord?

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
    assigneeUserId: UUID?
  ): TaskRecord =
    dbQuery {
      val newId = UUID.randomUUID()
      val now = nowTruncatedToMicros()
      TasksTable.insert {
        it[id] = newId
        it[TasksTable.productionId] = productionId
        it[TasksTable.title] = title
        it[TasksTable.description] = description
        it[TasksTable.dueDate] = dueDate
        it[status] = TaskStatus.OPEN.name
        it[TasksTable.assigneeUserId] = assigneeUserId
        it[createdAt] = now
      }
      val prodTitle = ProductionsTable
        .selectAll()
        .where { ProductionsTable.id eq productionId }
        .singleOrNull()
        ?.get(ProductionsTable.title) ?: ""
      TaskRecord(
        id = newId,
        productionId = productionId,
        productionTitle = prodTitle,
        title = title,
        description = description,
        dueDate = dueDate,
        status = TaskStatus.OPEN,
        priority = TaskPriority.MEDIUM,
        assigneeUserId = assigneeUserId,
        assigneeName = null,
        assigneeAvatarColorHex = null,
        createdAt = now
      )
    }

  override suspend fun findById(id: UUID): TaskRecord? =
    dbQuery {
      tasksWithRelations
        .selectAll()
        .where { TasksTable.id eq id }
        .singleOrNull()
        ?.toRecord()
    }

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

  // Non-deleted productions the user owns or is a member of. Must be called
  // inside a dbQuery transaction.
  private fun accessibleProductionIds(userId: UUID): List<UUID> {
    val memberProductionIds =
      ProductionMembersTable
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
