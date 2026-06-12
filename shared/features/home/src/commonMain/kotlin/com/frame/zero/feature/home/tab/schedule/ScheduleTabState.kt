package com.frame.zero.feature.home.tab.schedule

import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.feature.home.LoadErrorKind
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

data class ScheduleTabState(
  val isLoading: Boolean = false,
  val view: ScheduleView = ScheduleView.DAY,
  val schedule: Schedule? = null,
  val selectedDate: LocalDate? = null,
  val isSelectedDateToday: Boolean = false,
  val selectedDayEvents: ImmutableList<ScheduleEventUiModel> = persistentListOf(),
  val selectedDayTasks: ImmutableList<ScheduleTaskUiModel> = persistentListOf(),
  val error: LoadErrorKind? = null,
  /** Displayed year in the month-calendar view. Persisted across tab switches. */
  val displayYear: Int = 0,
  /** Displayed month in the month-calendar view. Persisted across tab switches. */
  val displayMonth: Month = Month.JANUARY
)
