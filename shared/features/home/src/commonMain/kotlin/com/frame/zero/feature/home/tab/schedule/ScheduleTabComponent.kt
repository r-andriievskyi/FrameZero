package com.frame.zero.feature.home.tab.schedule

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.frame.zero.domain.schedule.ScheduleView
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

class ScheduleTabComponent(
  componentContext: ComponentContext,
  viewModelFactory: () -> ScheduleTabViewModel
) : ComponentContext by componentContext {
  private val viewModel: ScheduleTabViewModel = instanceKeeper.getOrCreate { viewModelFactory() }

  val state: StateFlow<ScheduleTabState>
    get() = viewModel.state

  fun onViewChanged(view: ScheduleView) = viewModel.onViewChanged(view)

  fun onDateSelected(date: LocalDate) = viewModel.onDateSelected(date)

  fun onMonthNavigated(offset: Int) = viewModel.onMonthNavigated(offset)
}
