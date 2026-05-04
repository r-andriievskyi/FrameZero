package com.frame.zero.repository.schedule

import com.frame.zero.dto.schedule.ScheduleResponse

interface ScheduleRepository {
  suspend fun getSchedule(view: String, date: String): ScheduleResponse
}
