package com.frame.zero.feature.production.details

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductionDetailsComponent(
  componentContext: ComponentContext,
  val productionId: String,
  val onBack: () -> Unit,
  val onDeleted: (productionId: String) -> Unit,
  viewModelFactory: (productionId: String) -> ProductionDetailsViewModel
) : ComponentContext by componentContext {
  private val viewModel: ProductionDetailsViewModel =
    instanceKeeper.getOrCreate { viewModelFactory(productionId) }

  val state: StateFlow<ProductionDetailsState>
    get() = viewModel.state

  init {
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    lifecycle.doOnDestroy { scope.cancel() }
    scope.launch {
      viewModel.events.collect { event ->
        when (event) {
          is ProductionDetailsEvent.Deleted -> onDeleted(event.productionId)
        }
      }
    }
  }

  fun onIntent(intent: ProductionDetailsIntent) = viewModel.onIntent(intent)
}
