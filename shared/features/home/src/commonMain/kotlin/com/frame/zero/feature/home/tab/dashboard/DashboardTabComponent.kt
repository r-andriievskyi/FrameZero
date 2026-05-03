package com.frame.zero.feature.home.tab.dashboard

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.StateFlow

class DashboardTabComponent(componentContext: ComponentContext) :
  ComponentContext by componentContext {
  private val viewModel: DashboardTabViewModel = instanceKeeper.getOrCreate {
    DashboardTabViewModel()
  }

  val state: StateFlow<DashboardTabState>
    get() = viewModel.state

  /** Forwarded from a `LaunchedEffect` in the UI when this tab's pager page first composes. */
  fun onAppeared() = viewModel.onAppeared()
}
