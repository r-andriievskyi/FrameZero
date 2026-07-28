package com.frame.zero.feature.home.tab.schedule

import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.ui.UiText
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
  val error: UiText? = null,
  val isOffline: Boolean = false,
  val displayYear: Int = 0,
  val displayMonth: Month = Month.JANUARY
)
