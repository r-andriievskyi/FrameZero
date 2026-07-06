package com.frame.zero.feature.task.details.usecase

import com.frame.zero.domain.UseCase
import com.frame.zero.domain.task.TaskDetail
import com.frame.zero.repository.tasks.TasksRepository

class UpdateTaskParticipantsUseCase(
  private val tasksRepository: TasksRepository
) : UseCase<UpdateTaskParticipantsUseCase.Params, TaskDetail>() {
  data class Params(
    val taskId: String,
    val participantUserIds: List<String>
  )

  override suspend fun execute(params: Params): TaskDetail =
    tasksRepository.updateParticipants(params.taskId, params.participantUserIds)
}
