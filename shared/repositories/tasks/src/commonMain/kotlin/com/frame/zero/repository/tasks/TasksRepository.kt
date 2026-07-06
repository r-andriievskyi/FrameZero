package com.frame.zero.repository.tasks

import com.frame.zero.domain.Outcome
import com.frame.zero.domain.task.NewTask
import com.frame.zero.domain.task.TaskDetail
import com.frame.zero.domain.task.TaskSummary

interface TasksRepository {
  suspend fun getTask(id: String): TaskDetail

  suspend fun completeTask(id: String): TaskDetail

  suspend fun createTask(task: NewTask): TaskDetail

  /** Replaces the task's participant set; returns the updated detail. */
  suspend fun updateParticipants(
    taskId: String,
    userIds: List<String>
  ): TaskDetail

  suspend fun downloadAttachment(
    taskId: String,
    fileName: String,
    expectedBytes: Long
  ): Outcome<String>

  suspend fun listForProduction(productionId: String): List<TaskSummary>
}
