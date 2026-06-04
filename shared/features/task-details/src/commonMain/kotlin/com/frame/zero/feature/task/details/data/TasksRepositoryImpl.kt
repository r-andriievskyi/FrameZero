package com.frame.zero.feature.task.details.data

import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.dto.task.UpdateTaskRequest
import com.frame.zero.repository.tasks.TasksRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
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
}
