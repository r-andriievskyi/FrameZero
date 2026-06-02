package com.frame.zero.feature.home.tab.schedule

import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.domain.schedule.ScheduleView
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

data class ScheduleTabState(
  val isLoading: Boolean = false,
  val view: ScheduleView = ScheduleView.DAY,
  val schedule: Schedule? = null,
  val selectedDate: LocalDate? = null,
  val isSelectedDateToday: Boolean = false,
  val selectedDayEvents: List<ScheduleEventUiModel> = emptyList(),
  val selectedDayTasks: List<ScheduleTaskUiModel> = emptyList(),
  val error: String? = null,
  /** Displayed year in the month-calendar view. Persisted across tab switches. */
  val displayYear: Int = 0,
  /** Displayed month in the month-calendar view. Persisted across tab switches. */
  val displayMonth: Month = Month.JANUARY,
)
