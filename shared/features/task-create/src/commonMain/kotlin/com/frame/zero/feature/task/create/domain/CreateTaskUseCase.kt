package com.frame.zero.feature.task.create.domain

import com.frame.zero.domain.UseCase
import com.frame.zero.domain.task.NewTask
import com.frame.zero.domain.task.TaskDetail
import com.frame.zero.domain.task.TaskPriority
import com.frame.zero.repository.tasks.TasksRepository
import kotlinx.datetime.LocalDate

class CreateTaskUseCase(
  private val tasksRepository: TasksRepository
) : UseCase<CreateTaskUseCase.Params, TaskDetail>() {
  data class Params(
    val productionId: String,
    val title: String,
    val description: String?,
    val dueDate: LocalDate?,
    val assigneeUserId: String?,
    val priority: TaskPriority,
    val participantUserIds: List<String> = emptyList()
  )

  override suspend fun execute(params: Params): TaskDetail =
    tasksRepository.createTask(
      NewTask(
        productionId = params.productionId,
        title = params.title.trim(),
        description = params.description?.trim()?.ifBlank { null },
        dueDate = params.dueDate,
        assigneeUserId = params.assigneeUserId,
        priority = params.priority,
        participantUserIds = params.participantUserIds
      )
    )
}
