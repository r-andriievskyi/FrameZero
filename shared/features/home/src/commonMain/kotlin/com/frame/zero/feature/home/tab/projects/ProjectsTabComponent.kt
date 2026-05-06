package com.frame.zero.feature.home.tab.projects

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.StateFlow

class ProjectsTabComponent(
  componentContext: ComponentContext,
  val onCreateProductionClick: () -> Unit = {},
  viewModelFactory: () -> ProjectsTabViewModel
) : ComponentContext by componentContext {
  private val viewModel: ProjectsTabViewModel = instanceKeeper.getOrCreate { viewModelFactory() }

  val state: StateFlow<ProjectsTabState>
    get() = viewModel.state

  fun onAppeared() = viewModel.onAppeared()
}
