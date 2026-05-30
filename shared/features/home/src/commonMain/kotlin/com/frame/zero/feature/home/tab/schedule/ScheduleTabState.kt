package com.frame.zero.feature.home.tab.schedule

import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.domain.schedule.ScheduleView
import kotlinx.datetime.LocalDate

data class ScheduleTabState(
  val isLoading: Boolean = false,
  val view: ScheduleView = ScheduleView.DAY,
  val schedule: Schedule? = null,
  val selectedDate: LocalDate? = null,
  val error: String? = null
)
