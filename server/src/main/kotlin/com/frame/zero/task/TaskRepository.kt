package com.frame.zero.task

import com.frame.zero.common.decodeCursor
import com.frame.zero.common.encodeCursor
import com.frame.zero.config.dbQuery
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.production.ProductionMembersTable
import com.frame.zero.production.ProductionsTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class TaskRecord(
  val id: UUID,
  val productionId: UUID,
  val productionTitle: String,
  val title: String,
  val description: String?,
  val dueDate: LocalDate?,
  val status: TaskStatus,
  val assigneeUserId: UUID?,
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

class TaskRepositoryExposed : TaskRepository {
  override suspend fun create(
    productionId: UUID,
    title: String,
    description: String?,
    dueDate: LocalDate?,
    assigneeUserId: UUID?
  ): TaskRecord =
    dbQuery {
      val newId = UUID.randomUUID()
      val now = Instant.now()
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
      val prodTitle =
        ProductionsTable
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
        assigneeUserId = assigneeUserId,
        createdAt = now
      )
    }

  override suspend fun findById(id: UUID): TaskRecord? =
    dbQuery {
      (TasksTable innerJoin ProductionsTable)
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
      val rows =
        (TasksTable innerJoin ProductionsTable)
          .selectAll()
          .where {
            var cond = TasksTable.id.isNotNull()
            if (assigneeMe) cond = cond and (TasksTable.assigneeUserId eq userId)
            if (status != null) cond = cond and (TasksTable.status eq status.name)
            if (productionId != null) cond = cond and (TasksTable.productionId eq productionId)
            if (cursor != null) {
              val pc = decodeCursor(cursor)
              if (pc != null) {
                val cursorTs = Instant.ofEpochMilli(pc.epochMillis)
                cond =
                  cond and
                  (
                    (TasksTable.createdAt less cursorTs) or
                      ((TasksTable.createdAt eq cursorTs) and (TasksTable.id less pc.id))
                  )
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
          encodeCursor(last.createdAt.toEpochMilli(), last.id)
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
      (TasksTable innerJoin ProductionsTable)
        .selectAll()
        .where {
          (TasksTable.assigneeUserId eq userId) and (TasksTable.status eq TaskStatus.OPEN.name)
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
      val memberProductionIds =
        ProductionMembersTable
          .selectAll()
          .where { ProductionMembersTable.userId eq userId }
          .map { it[ProductionMembersTable.productionId] }
      if (memberProductionIds.isEmpty()) return@dbQuery emptyList()

      (TasksTable innerJoin ProductionsTable)
        .selectAll()
        .where {
          TasksTable.dueDate.isNotNull() and
            (TasksTable.dueDate greaterEq rangeStart) and
            (TasksTable.dueDate lessEq rangeEnd) and
            (TasksTable.productionId inList memberProductionIds)
        }.orderBy(TasksTable.dueDate to SortOrder.ASC, TasksTable.id to SortOrder.ASC)
        .map { it.toRecord() }
    }

  override suspend fun countOpenForUser(userId: UUID): Int =
    dbQuery {
      TasksTable
        .selectAll()
        .where {
          (TasksTable.assigneeUserId eq userId) and (TasksTable.status eq TaskStatus.OPEN.name)
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
      val updated =
        TasksTable.update({ TasksTable.id eq id }) { row ->
          title?.let { row[TasksTable.title] = it }
          description?.let { row[TasksTable.description] = it }
          dueDate?.let { row[TasksTable.dueDate] = it }
          status?.let { row[TasksTable.status] = it.name }
          assigneeUserId?.let { row[TasksTable.assigneeUserId] = it }
        }
      if (updated == 0) {
        null
      } else {
        (TasksTable innerJoin ProductionsTable)
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

  private fun ResultRow.toRecord(): TaskRecord =
    TaskRecord(
      id = this[TasksTable.id],
      productionId = this[TasksTable.productionId],
      productionTitle = this[ProductionsTable.title],
      title = this[TasksTable.title],
      description = this[TasksTable.description],
      dueDate = this[TasksTable.dueDate],
      status = TaskStatus.valueOf(this[TasksTable.status]),
      assigneeUserId = this[TasksTable.assigneeUserId],
      createdAt = this[TasksTable.createdAt]
    )
}
