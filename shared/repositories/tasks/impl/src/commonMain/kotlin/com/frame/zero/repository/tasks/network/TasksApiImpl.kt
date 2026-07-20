package com.frame.zero.repository.tasks.network

import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.dto.common.CursorPagedResponse
import com.frame.zero.dto.task.TaskSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class TasksApiImpl(
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig
) : TasksApi {
  override suspend fun getAll(
    limit: Int,
    cursor: String?
  ): CursorPagedResponse<TaskSummaryDto> =
    httpClient
      .get("${networkConfig.baseUrl}/api/v1/tasks") {
        parameter("limit", limit)
        parameter("cursor", cursor)
      }.body<CursorPagedResponse<TaskSummaryDto>>()
}
