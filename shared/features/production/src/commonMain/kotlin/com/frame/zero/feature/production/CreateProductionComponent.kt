package com.frame.zero.feature.production

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CreateProductionComponent(
  componentContext: ComponentContext,
  val onBack: () -> Unit,
  val onCreated: () -> Unit,
  viewModelFactory: () -> CreateProductionViewModel
) : ComponentContext by componentContext {
  private val viewModel: CreateProductionViewModel = instanceKeeper.getOrCreate { viewModelFactory() }
  private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  val state: StateFlow<CreateProductionState>
    get() = viewModel.state

  init {
    lifecycle.doOnDestroy { scope.cancel() }
    viewModel.navigationEvents
      .onEach { onCreated() }
      .launchIn(scope)
  }

  fun onIntent(intent: CreateProductionIntent) = viewModel.onIntent(intent)

  fun navigateBack() {
    if (viewModel.state.value.currentStep > 1) {
      viewModel.onIntent(CreateProductionIntent.PreviousStep)
    } else {
      onBack()
    }
  }
}
