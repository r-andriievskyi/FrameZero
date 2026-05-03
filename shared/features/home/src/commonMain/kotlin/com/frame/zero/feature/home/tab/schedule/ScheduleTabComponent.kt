package com.frame.zero.feature.home.tab.schedule

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.StateFlow

class ScheduleTabComponent(componentContext: ComponentContext) :
  ComponentContext by componentContext {
  private val viewModel: ScheduleTabViewModel = instanceKeeper.getOrCreate {
    ScheduleTabViewModel()
  }

  val state: StateFlow<ScheduleTabState>
    get() = viewModel.state

  fun onAppeared() = viewModel.onAppeared()
}
