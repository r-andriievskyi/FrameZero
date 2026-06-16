package com.frame.zero.repository.tasks

import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.dto.task.TaskSummaryDto

interface TasksRepository {
  suspend fun getTask(id: String): TaskDetailDto

  suspend fun completeTask(id: String): TaskDetailDto

  suspend fun createTask(request: CreateTaskRequest): TaskDetailDto

  suspend fun listForProduction(productionId: String): List<TaskSummaryDto>
}
