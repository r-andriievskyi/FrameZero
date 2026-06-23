package com.frame.zero.testing

import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.dto.task.TaskSummaryDto
import com.frame.zero.repository.tasks.TasksRepository

/**
 * Single configurable fake for [TasksRepository], shared across all client feature tests.
 * Construct with the data/errors a test needs; assert against the recorded call lists.
 */
class FakeTasksRepository(
  private val task: TaskDetailDto = taskDetailDto(),
  private val completedTask: TaskDetailDto = task,
  private val created: TaskDetailDto = task,
  private val tasks: List<TaskSummaryDto> = emptyList(),
  private val getThrows: Throwable? = null,
  private val completeThrows: Throwable? = null,
  private val createThrows: Throwable? = null,
  private val listThrows: Throwable? = null
) : TasksRepository {
  val getCalls: MutableList<String> = mutableListOf()
  val completeCalls: MutableList<String> = mutableListOf()
  val createRequests: MutableList<CreateTaskRequest> = mutableListOf()
  val listedProductionIds: MutableList<String> = mutableListOf()

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

  override suspend fun createTask(request: CreateTaskRequest): TaskDetailDto {
    createRequests += request
    createThrows?.let { throw it }
    return created
  }

  override suspend fun listForProduction(productionId: String): List<TaskSummaryDto> {
    listedProductionIds += productionId
    listThrows?.let { throw it }
    return tasks
  }
}
