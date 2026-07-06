package com.frame.zero.testing

import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.repository.schedule.ScheduleRepository
import kotlinx.datetime.LocalDate

class FakeScheduleRepository(
  private val schedule: Schedule =
    Schedule(
      rangeStart = LocalDate(2026, 1, 1),
      rangeEnd = LocalDate(2026, 1, 1),
      days = emptyList()
    ),
  private val throws: Throwable? = null
) : ScheduleRepository {
  data class Call(
    val view: ScheduleView,
    val date: LocalDate
  )

  val calls: MutableList<Call> = mutableListOf()

  override suspend fun getSchedule(
    view: ScheduleView,
    date: LocalDate
  ): Schedule {
    calls += Call(view, date)
    throws?.let { throw it }
    return schedule
  }
}
