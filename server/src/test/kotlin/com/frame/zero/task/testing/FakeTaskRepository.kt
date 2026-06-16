package com.frame.zero.task.testing

import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.task.TaskRecord
import com.frame.zero.task.TaskRepository
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import java.util.UUID

internal class FakeTaskRepository : TaskRepository {
  val tasks: MutableList<TaskRecord> = mutableListOf()

  override suspend fun create(
    productionId: UUID,
    title: String,
    description: String?,
    dueDate: LocalDate?,
    assigneeUserId: UUID?,
    priority: TaskPriority
  ): TaskRecord {
    val record =
      TaskRecord(
        id = UUID.randomUUID(),
        productionId = productionId,
        productionTitle = "Test Production",
        title = title,
        description = description,
        dueDate = dueDate,
        status = TaskStatus.OPEN,
        priority = priority,
        assigneeUserId = assigneeUserId,
        assigneeName = null,
        assigneeAvatarColorHex = null,
        createdAt = Clock.System.now()
      )
    tasks += record
    return record
  }

  override suspend fun findById(id: UUID): TaskRecord? = tasks.firstOrNull { it.id == id }

  override suspend fun findForUser(
    userId: UUID,
    assigneeMe: Boolean,
    status: TaskStatus?,
    productionId: UUID?,
    limit: Int,
    cursor: String?
  ): Pair<List<TaskRecord>, String?> {
    val items =
      tasks
        .filter { t ->
          (!assigneeMe || t.assigneeUserId == userId) &&
            (status == null || t.status == status) &&
            (productionId == null || t.productionId == productionId)
        }.take(limit)
    return Pair(items, null)
  }

  override suspend fun findForUserLimit(
    userId: UUID,
    limit: Int
  ): List<TaskRecord> = tasks.filter { it.assigneeUserId == userId && it.status == TaskStatus.OPEN }.take(limit)

  override suspend fun findInRangeForUser(
    userId: UUID,
    rangeStart: LocalDate,
    rangeEnd: LocalDate
  ): List<TaskRecord> =
    tasks.filter { t ->
      val due = t.dueDate ?: return@filter false
      due >= rangeStart && due <= rangeEnd
    }

  override suspend fun countOpenForUser(userId: UUID): Int =
    tasks.count {
      it.assigneeUserId == userId && it.status == TaskStatus.OPEN
    }

  override suspend fun update(
    id: UUID,
    title: String?,
    description: String?,
    dueDate: LocalDate?,
    status: TaskStatus?,
    assigneeUserId: UUID?
  ): TaskRecord? {
    val idx = tasks.indexOfFirst { it.id == id }
    if (idx < 0) return null
    val updated =
      tasks[idx].copy(
        title = title ?: tasks[idx].title,
        description = description ?: tasks[idx].description,
        dueDate = dueDate ?: tasks[idx].dueDate,
        status = status ?: tasks[idx].status,
        assigneeUserId = assigneeUserId ?: tasks[idx].assigneeUserId
      )
    tasks[idx] = updated
    return updated
  }

  override suspend fun delete(id: UUID): Boolean {
    val idx = tasks.indexOfFirst { it.id == id }
    if (idx < 0) return false
    tasks.removeAt(idx)
    return true
  }
}
