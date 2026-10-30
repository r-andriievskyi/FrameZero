package com.frame.zero.feature.production.details

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnResume
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
  val onAddTask: (
    productionId: String,
    productionTitle: String
  ) -> Unit,
  viewModelFactory: (productionId: String) -> ProductionDetailsViewModel
) : ComponentContext by componentContext {
  private val viewModel: ProductionDetailsViewModel =
    instanceKeeper.getOrCreate { viewModelFactory(productionId) }

  val state: StateFlow<ProductionDetailsState>
    get() = viewModel.state

  // The ViewModel already loads tasks on creation, so the first resume is a no-op;
  // subsequent resumes (e.g. returning from the create-task screen) refresh the list.
  private var skipNextResume = true

  init {
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    lifecycle.doOnDestroy { scope.cancel() }
    lifecycle.doOnResume {
      if (skipNextResume) {
        skipNextResume = false
      } else {
        viewModel.onIntent(ProductionDetailsIntent.RefreshTasks)
      }
    }
    scope.launch {
      viewModel.events.collect { event ->
        when (event) {
          is ProductionDetailsEvent.Deleted -> onDeleted(event.productionId)
          is ProductionDetailsEvent.AddTaskRequested ->
            onAddTask(event.productionId, event.productionTitle)
        }
      }
    }
  }

  fun onIntent(intent: ProductionDetailsIntent) = viewModel.onIntent(intent)
}
