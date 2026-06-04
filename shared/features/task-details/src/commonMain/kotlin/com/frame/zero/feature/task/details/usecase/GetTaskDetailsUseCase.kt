package com.frame.zero.feature.task.details.usecase

import com.frame.zero.domain.DomainError
import com.frame.zero.domain.UseCase
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.repository.tasks.TasksRepository
import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException

class GetTaskDetailsUseCase(
  private val tasksRepository: TasksRepository
) : UseCase<String, TaskDetailDto>() {
  override fun mapError(throwable: Throwable): DomainError =
    when (throwable) {
      is IOException -> DomainError.Network(throwable.message ?: "Network error")
      is ResponseException -> DomainError.Unknown(throwable.message)
      else -> DomainError.Unknown(throwable.message)
    }

  override suspend fun execute(params: String): TaskDetailDto = tasksRepository.getTask(params)
}
