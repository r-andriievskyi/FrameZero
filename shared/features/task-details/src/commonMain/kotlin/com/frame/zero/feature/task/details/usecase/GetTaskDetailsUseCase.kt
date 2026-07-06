package com.frame.zero.feature.task.details.usecase

import com.frame.zero.domain.UseCase
import com.frame.zero.domain.task.TaskDetail
import com.frame.zero.repository.tasks.TasksRepository

class GetTaskDetailsUseCase(
  private val tasksRepository: TasksRepository
) : UseCase<String, TaskDetail>() {
  override suspend fun execute(params: String): TaskDetail = tasksRepository.getTask(params)
}
