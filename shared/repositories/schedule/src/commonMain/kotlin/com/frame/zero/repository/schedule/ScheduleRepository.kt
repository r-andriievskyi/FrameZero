package com.frame.zero.repository.schedule

import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.domain.schedule.ScheduleView
import kotlinx.datetime.LocalDate

interface ScheduleRepository {
  suspend fun getSchedule(
    view: ScheduleView,
    date: LocalDate
  ): Schedule
}
