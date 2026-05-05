package com.frame.zero.feature.home.tab.dashboard

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.StateFlow

class DashboardTabComponent(
  componentContext: ComponentContext,
  viewModelFactory: () -> DashboardTabViewModel
) : ComponentContext by componentContext {
  private val viewModel: DashboardTabViewModel = instanceKeeper.getOrCreate { viewModelFactory() }

  val state: StateFlow<DashboardTabState>
    get() = viewModel.state

  fun onAppeared() = viewModel.onAppeared()
}
