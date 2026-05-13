package com.frame.zero.feature.home.tab.projects

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.frame.zero.domain.production.ProductionPhase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class ProjectsTabComponent(
  componentContext: ComponentContext,
  val onCreateProductionClick: () -> Unit = {},
  val onProductionClick: (productionId: String) -> Unit = {},
  viewModelFactory: () -> ProjectsTabViewModel
) : ComponentContext by componentContext {
  private val viewModel: ProjectsTabViewModel = instanceKeeper.getOrCreate { viewModelFactory() }

  val state: StateFlow<ProjectsTabState>
    get() = viewModel.state

  val productions: Flow<PagingData<ProductionUi>>
    get() = viewModel.productions

  fun onFilterSelected(phase: ProductionPhase?) = viewModel.onFilterSelected(phase)
}
