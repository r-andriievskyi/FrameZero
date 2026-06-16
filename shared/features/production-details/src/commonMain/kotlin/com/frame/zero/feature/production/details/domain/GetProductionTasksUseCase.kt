package com.frame.zero.feature.production.details.domain

import com.frame.zero.domain.UseCase
import com.frame.zero.repository.tasks.TasksRepository

class GetProductionTasksUseCase(
  private val tasksRepository: TasksRepository
) : UseCase<GetProductionTasksUseCase.Params, List<ProductionTask>>() {
  data class Params(
    val productionId: String
  )

  override suspend fun execute(params: Params): List<ProductionTask> =
    tasksRepository
      .listForProduction(params.productionId)
      .map { it.toProductionTask() }
}
