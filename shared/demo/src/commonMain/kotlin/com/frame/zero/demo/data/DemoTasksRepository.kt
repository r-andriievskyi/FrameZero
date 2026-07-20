package com.frame.zero.demo.data

import androidx.paging.PagingData
import com.frame.zero.demo.DemoDataStore
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.task.NewTask
import com.frame.zero.domain.task.TaskAssignee
import com.frame.zero.domain.task.TaskDetail
import com.frame.zero.domain.task.TaskStatus
import com.frame.zero.domain.task.TaskSummary
import com.frame.zero.repository.tasks.TasksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

internal class DemoTasksRepository(
  private val store: DemoDataStore
) : TasksRepository {
  override suspend fun getTask(id: String): TaskDetail = store.getTask(id) ?: error("Unknown demo task $id")

  override suspend fun completeTask(id: String): TaskDetail = store.completeTask(id) ?: error("Unknown demo task $id")

  override suspend fun createTask(task: NewTask): TaskDetail {
    val now = Clock.System.now()
    val production = store.getProduction(task.productionId)
    val assigneeMember = task.assigneeUserId?.let { userId ->
      production?.keyCrew?.firstOrNull { it.userId == userId }
    }
    val detail = TaskDetail(
      id = "demo-task-${now.toEpochMilliseconds()}",
      productionId = task.productionId,
      productionTitle = production?.title.orEmpty(),
      title = task.title,
      description = task.description,
      dueDate = task.dueDate,
      status = TaskStatus.OPEN,
      priority = task.priority,
      assigneeUserId = task.assigneeUserId,
      assignee = assigneeMember?.let {
        TaskAssignee(userId = it.userId!!, name = it.name, avatarColorHex = it.avatarColorHex)
      },
      createdAt = now
    )
    store.addTask(detail)
    return store.updateParticipants(detail.id, task.participantUserIds) ?: detail
  }

  override suspend fun updateParticipants(
    taskId: String,
    userIds: List<String>
  ): TaskDetail = store.updateParticipants(taskId, userIds) ?: error("Unknown demo task $taskId")

  // Demo tasks ship without attachments, so this is never reached from the UI.
  override suspend fun downloadAttachment(
    taskId: String,
    fileName: String,
    expectedBytes: Long
  ): Outcome<String> = Outcome.Failure(DomainError.NotFound)

  override suspend fun listForProduction(productionId: String): List<TaskSummary> =
    store.tasksForProduction(productionId).map { it.toSummary() }

  override fun observeUserTasks(): Flow<PagingData<TaskSummary>> =
    store.tasks.map { list -> PagingData.from(list.map { it.toSummary() }) }
}

private fun TaskDetail.toSummary(): TaskSummary = TaskSummary(
  id = id,
  title = title,
  productionTitle = productionTitle,
  dueDate = dueDate,
  status = status
)
