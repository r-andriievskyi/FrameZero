package com.frame.zero.feature.home.data

import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.domain.schedule.toDomain
import com.frame.zero.dto.schedule.ScheduleResponse
import com.frame.zero.repository.schedule.ScheduleRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

class ScheduleRepositoryImpl(
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig
) : ScheduleRepository {
  override suspend fun getSchedule(
    view: ScheduleView,
    date: LocalDate
  ): Schedule {
    // The month endpoint takes "yyyy-MM"; day/week take a full ISO date.
    val dateParam =
      when (view) {
        ScheduleView.MONTH -> "${date.year}-${date.month.number.toString().padStart(2, '0')}"
        else -> date.toString()
      }
    return httpClient
      .get("${networkConfig.baseUrl}/api/v1/schedule") {
        parameter("view", view.name.lowercase())
        parameter("date", dateParam)
      }.body<ScheduleResponse>()
      .toDomain()
  }
}
