package com.frame.zero.feature.task.details.data

import com.frame.zero.core.network.NetworkConfig
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

class TasksRepositoryImpl(
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig
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
