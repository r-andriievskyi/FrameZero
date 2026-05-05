package com.frame.zero.feature.home.data

import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.dto.schedule.ScheduleResponse
import com.frame.zero.repository.schedule.ScheduleRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class ScheduleRepositoryImpl(
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig
) : ScheduleRepository {
  override suspend fun getSchedule(
    view: String,
    date: String
  ): ScheduleResponse =
    httpClient
      .get("${networkConfig.baseUrl}/api/v1/schedule") {
        parameter("view", view)
        parameter("date", date)
      }.body()
}
