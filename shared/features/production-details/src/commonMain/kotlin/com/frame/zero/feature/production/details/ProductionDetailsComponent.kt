package com.frame.zero.feature.production.details

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.StateFlow

class ProductionDetailsComponent(
  componentContext: ComponentContext,
  val productionId: String,
  val onBack: () -> Unit,
  viewModelFactory: (productionId: String) -> ProductionDetailsViewModel
) : ComponentContext by componentContext {
  private val viewModel: ProductionDetailsViewModel =
    instanceKeeper.getOrCreate { viewModelFactory(productionId) }

  val state: StateFlow<ProductionDetailsState>
    get() = viewModel.state

  fun onIntent(intent: ProductionDetailsIntent) = viewModel.onIntent(intent)
}
