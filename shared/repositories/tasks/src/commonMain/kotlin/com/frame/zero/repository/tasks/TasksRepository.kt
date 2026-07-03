package com.frame.zero.repository.tasks

import com.frame.zero.domain.Outcome
import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.dto.task.TaskSummaryDto

interface TasksRepository {
  suspend fun getTask(id: String): TaskDetailDto

  suspend fun completeTask(id: String): TaskDetailDto

  suspend fun createTask(request: CreateTaskRequest): TaskDetailDto

  /** Replaces the task's participant set; returns the updated detail. */
  suspend fun updateParticipants(
    taskId: String,
    userIds: List<String>
  ): TaskDetailDto

  suspend fun downloadAttachment(
    taskId: String,
    fileName: String,
    expectedBytes: Long
  ): Outcome<String>

  suspend fun listForProduction(productionId: String): List<TaskSummaryDto>
}
