package com.frame.zero.feature.task.details.testing

import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.repository.tasks.TasksRepository

internal class FakeTasksRepository(
  private val task: TaskDetailDto,
  private val getThrows: Throwable? = null,
  private val completeThrows: Throwable? = null,
  private val completedTask: TaskDetailDto = task
) : TasksRepository {
  val getCalls: MutableList<String> = mutableListOf()
  val completeCalls: MutableList<String> = mutableListOf()

  override suspend fun getTask(id: String): TaskDetailDto {
    getCalls += id
    getThrows?.let { throw it }
    return task
  }

  override suspend fun completeTask(id: String): TaskDetailDto {
    completeCalls += id
    completeThrows?.let { throw it }
    return completedTask
  }
}
