package com.frame.zero.feature.home.tab.productions

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class ProductionsTabComponent(
  componentContext: ComponentContext,
  val onCreateProductionClick: () -> Unit,
  val onProductionClick: (productionId: String) -> Unit,
  viewModelFactory: () -> ProductionsTabViewModel
) : ComponentContext by componentContext {
  private val viewModel: ProductionsTabViewModel = instanceKeeper.getOrCreate { viewModelFactory() }

  val state: StateFlow<ProductionsTabState>
    get() = viewModel.state

  val productions: Flow<PagingData<ProductionUi>>
    get() = viewModel.productions

  fun onFilterSelected(filter: ProductionFilter) = viewModel.onFilterSelected(filter)
}
