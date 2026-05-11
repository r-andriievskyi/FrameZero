package com.frame.zero.feature.production.details

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.Outcome
import com.frame.zero.feature.production.details.domain.DeleteProductionUseCase
import com.frame.zero.feature.production.details.domain.GetProductionDetailsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ProductionDetailsViewModel(
  private val productionId: String,
  private val getProductionDetailsUseCase: GetProductionDetailsUseCase,
  private val deleteProductionUseCase: DeleteProductionUseCase,
  dispatcher: CoroutineContext = Dispatchers.Main
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(ProductionDetailsState())
  val state: StateFlow<ProductionDetailsState> = _state.asStateFlow()

  private val _events =
    MutableSharedFlow<ProductionDetailsEvent>(
      extraBufferCapacity = 1,
      onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
  val events: SharedFlow<ProductionDetailsEvent> = _events.asSharedFlow()

  init {
    load()
  }

  fun onIntent(intent: ProductionDetailsIntent) {
    when (intent) {
      ProductionDetailsIntent.Refresh -> load()
      ProductionDetailsIntent.DeleteRequested ->
        _state.update { it.copy(isDeleteDialogVisible = true, deleteError = null) }
      ProductionDetailsIntent.DeleteDismissed ->
        _state.update { it.copy(isDeleteDialogVisible = false) }
      ProductionDetailsIntent.DeleteConfirmed -> deleteProduction()
      ProductionDetailsIntent.DeleteErrorDismissed ->
        _state.update { it.copy(deleteError = null) }
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

  private fun deleteProduction() {
    if (_state.value.isDeleting) return
    scope.launch {
      _state.update {
        it.copy(isDeleting = true, isDeleteDialogVisible = false, deleteError = null)
      }
      val params = DeleteProductionUseCase.Params(productionId = productionId)
      when (val outcome = deleteProductionUseCase(params)) {
        is Outcome.Success -> {
          _state.update { it.copy(isDeleting = false) }
          _events.tryEmit(ProductionDetailsEvent.Deleted(productionId))
        }
        is Outcome.Failure ->
          _state.update {
            it.copy(isDeleting = false, deleteError = outcome.error.toString())
          }
      }
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
