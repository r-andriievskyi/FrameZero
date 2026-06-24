package com.frame.zero.feature.task.details.data

import com.frame.zero.core.files.AttachmentFileManager
import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.core.network.connectivity.ConnectivityObserver
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.toDomainError
import com.frame.zero.dto.common.CursorPagedResponse
import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.dto.task.TaskSummaryDto
import com.frame.zero.dto.task.UpdateTaskRequest
import com.frame.zero.repository.tasks.TasksRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.readRawBytes

class TasksRepositoryImpl(
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig,
  private val connectivityObserver: ConnectivityObserver,
  private val attachmentFileManager: AttachmentFileManager
) : TasksRepository {
  override suspend fun getTask(id: String): TaskDetailDto =
    httpClient.get("${networkConfig.baseUrl}/api/v1/tasks/$id").body()

  override suspend fun completeTask(id: String): TaskDetailDto =
    httpClient
      .patch("${networkConfig.baseUrl}/api/v1/tasks/$id") {
        setBody(UpdateTaskRequest(status = TaskStatus.DONE))
      }.body()

  override suspend fun createTask(request: CreateTaskRequest): TaskDetailDto =
    httpClient
      .post("${networkConfig.baseUrl}/api/v1/tasks") {
        setBody(request)
      }.body()

  override suspend fun downloadAttachment(
    taskId: String,
    fileName: String,
    expectedBytes: Long
  ): Outcome<String> {
    attachmentFileManager.cachedAttachment(taskId, fileName)?.let { return Outcome.Success(it) }
    if (!connectivityObserver.isCurrentlyOnline()) {
      return Outcome.Failure(DomainError.Offline("No internet connection"))
    }
    if (attachmentFileManager.availableBytes() < expectedBytes) {
      return Outcome.Failure(DomainError.InsufficientStorage)
    }
    return runCatching {
      val bytes = httpClient
        .get("${networkConfig.baseUrl}/api/v1/tasks/$taskId/attachment")
        .readRawBytes()
      attachmentFileManager.saveDownloaded(taskId, fileName, bytes)
    }.fold(
      onSuccess = { Outcome.Success(it) },
      onFailure = { Outcome.Failure(it.toDomainError()) }
    )
  }

  override suspend fun listForProduction(productionId: String): List<TaskSummaryDto> =
    httpClient
      .get("${networkConfig.baseUrl}/api/v1/tasks") {
        parameter("productionId", productionId)
        parameter("limit", PRODUCTION_TASKS_PAGE_SIZE)
      }.body<CursorPagedResponse<TaskSummaryDto>>()
      .items

  private companion object {
    // The production-details card shows a single page of recent tasks; it is not
    // a full paginated list, so we fetch the first page and ignore the cursor.
    const val PRODUCTION_TASKS_PAGE_SIZE = 50
  }
}
