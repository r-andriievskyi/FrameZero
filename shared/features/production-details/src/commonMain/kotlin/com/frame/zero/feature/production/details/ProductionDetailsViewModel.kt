package com.frame.zero.feature.production.details

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.Outcome
import com.frame.zero.feature.production.details.domain.GetProductionDetailsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ProductionDetailsViewModel(
  private val productionId: String,
  private val getProductionDetailsUseCase: GetProductionDetailsUseCase,
  dispatcher: CoroutineContext = Dispatchers.Main
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(ProductionDetailsState())
  val state: StateFlow<ProductionDetailsState> = _state.asStateFlow()

  init {
    load()
  }

  fun onIntent(intent: ProductionDetailsIntent) {
    when (intent) {
      ProductionDetailsIntent.Refresh -> load()
    }
  }

  private fun load() {
    scope.launch {
      _state.update { it.copy(isLoading = true, error = null) }
      val params = GetProductionDetailsUseCase.Params(productionId = productionId)
      when (val outcome = getProductionDetailsUseCase(params)) {
        is Outcome.Success ->
          _state.update { it.copy(isLoading = false, detail = outcome.data) }
        is Outcome.Failure ->
          _state.update { it.copy(isLoading = false, error = outcome.error.toString()) }
      }
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
