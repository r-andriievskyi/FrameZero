package com.frame.zero.repository.tasks

import com.frame.zero.dto.task.TaskDetailDto

interface TasksRepository {
  suspend fun getTask(id: String): TaskDetailDto

  suspend fun completeTask(id: String): TaskDetailDto
}
