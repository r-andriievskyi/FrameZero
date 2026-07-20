package com.frame.zero.testing

import androidx.paging.PagingData
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.task.NewTask
import com.frame.zero.domain.task.TaskDetail
import com.frame.zero.domain.task.TaskSummary
import com.frame.zero.repository.tasks.TasksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Single configurable fake for [TasksRepository], shared across all client feature tests.
 * Construct with the data/errors a test needs; assert against the recorded call lists.
 */
class FakeTasksRepository(
  private val task: TaskDetail = taskDetail(),
  private val completedTask: TaskDetail = task,
  private val created: TaskDetail = task,
  private val updatedParticipantsTask: TaskDetail? = null,
  private val tasks: List<TaskSummary> = emptyList(),
  private val getThrows: Throwable? = null,
  private val completeThrows: Throwable? = null,
  private val createThrows: Throwable? = null,
  private val updateParticipantsThrows: Throwable? = null,
  private val listThrows: Throwable? = null,
  private val downloadedPath: String = "/local/attachment",
  private val downloadError: DomainError? = null
) : TasksRepository {
  val getCalls: MutableList<String> = mutableListOf()
  val completeCalls: MutableList<String> = mutableListOf()
  val createRequests: MutableList<NewTask> = mutableListOf()
  val updateParticipantsCalls: MutableList<Pair<String, List<String>>> = mutableListOf()
  val downloadCalls: MutableList<String> = mutableListOf()
  val listedProductionIds: MutableList<String> = mutableListOf()

  override suspend fun getTask(id: String): TaskDetail {
    getCalls += id
    getThrows?.let { throw it }
    return task
  }

  override suspend fun completeTask(id: String): TaskDetail {
    completeCalls += id
    completeThrows?.let { throw it }
    return completedTask
  }

  override suspend fun createTask(task: NewTask): TaskDetail {
    createRequests += task
    createThrows?.let { throw it }
    return created
  }

  override suspend fun updateParticipants(
    taskId: String,
    userIds: List<String>
  ): TaskDetail {
    updateParticipantsCalls += taskId to userIds
    updateParticipantsThrows?.let { throw it }
    return updatedParticipantsTask ?: task
  }

  override suspend fun downloadAttachment(
    taskId: String,
    fileName: String,
    expectedBytes: Long
  ): Outcome<String> {
    downloadCalls += taskId
    return downloadError?.let { Outcome.Failure(it) } ?: Outcome.Success(downloadedPath)
  }

  override suspend fun listForProduction(productionId: String): List<TaskSummary> {
    listedProductionIds += productionId
    listThrows?.let { throw it }
    return tasks
  }

  override fun observeUserTasks(): Flow<PagingData<TaskSummary>> = flowOf(PagingData.from(tasks))
}
