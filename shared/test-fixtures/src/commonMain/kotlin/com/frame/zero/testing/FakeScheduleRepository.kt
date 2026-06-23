package com.frame.zero.testing

import com.frame.zero.dto.schedule.ScheduleResponse
import com.frame.zero.repository.schedule.ScheduleRepository
import kotlinx.datetime.LocalDate

class FakeScheduleRepository(
  private val response: ScheduleResponse =
    ScheduleResponse(
      rangeStart = LocalDate(2026, 1, 1),
      rangeEnd = LocalDate(2026, 1, 1),
      days = emptyList()
    ),
  private val throws: Throwable? = null
) : ScheduleRepository {
  data class Call(
    val view: String,
    val date: String
  )

  val calls: MutableList<Call> = mutableListOf()

  override suspend fun getSchedule(
    view: String,
    date: String
  ): ScheduleResponse {
    calls += Call(view, date)
    throws?.let { throw it }
    return response
  }
}
