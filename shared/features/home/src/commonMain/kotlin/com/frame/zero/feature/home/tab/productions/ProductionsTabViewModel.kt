package com.frame.zero.feature.home.tab.productions

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.repository.productions.ProductionsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class ProductionsTabViewModel(
  private val productionsRepository: ProductionsRepository,
  dispatcher: CoroutineContext = Dispatchers.Main
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(ProductionsTabState())
  val state: StateFlow<ProductionsTabState> = _state.asStateFlow()

  val productions: Flow<PagingData<ProductionUi>> = _state
    .flatMapLatest { current -> productionsRepository.observeProductions(current.selectedFilter) }
    .map { page -> page.map { it.toUi() } }
    .cachedIn(scope)

  fun onFilterSelected(phase: ProductionPhase?) {
    if (_state.value.selectedFilter == phase) return
    _state.value = _state.value.copy(selectedFilter = phase)
  }

  override fun onDestroy() {
    scope.cancel()
  }
}

