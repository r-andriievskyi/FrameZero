package com.frame.zero.feature.home.tab.schedule

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.StateFlow

class ScheduleTabComponent(
  componentContext: ComponentContext,
  val onTaskClick: (taskId: String) -> Unit = {},
  viewModelFactory: () -> ScheduleTabViewModel
) : ComponentContext by componentContext {
  private val viewModel: ScheduleTabViewModel = instanceKeeper.getOrCreate { viewModelFactory() }

  val state: StateFlow<ScheduleTabState>
    get() = viewModel.state

  fun onIntent(intent: ScheduleTabIntent) = viewModel.onIntent(intent)
}
