package com.frame.zero.feature.task.details.usecase

import com.frame.zero.domain.UseCase
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.repository.tasks.TasksRepository

class CompleteTaskUseCase(
  private val tasksRepository: TasksRepository
) : UseCase<String, TaskDetailDto>() {
  override suspend fun execute(params: String): TaskDetailDto = tasksRepository.completeTask(params)
}
