package com.frame.zero.feature.home.tab.schedule

import com.frame.zero.domain.schedule.ScheduleView
import kotlinx.datetime.LocalDate

sealed interface ScheduleTabIntent {
  data class ViewChanged(
    val view: ScheduleView
  ) : ScheduleTabIntent

  data class DateSelected(
    val date: LocalDate
  ) : ScheduleTabIntent

  data class MonthNavigated(
    val offset: Int
  ) : ScheduleTabIntent

  data object Retry : ScheduleTabIntent
}
