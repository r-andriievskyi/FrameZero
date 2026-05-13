package com.frame.zero.feature.home.tab.projects

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.Outcome
import com.frame.zero.feature.home.usecase.GetProductionsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * See [com.frame.zero.feature.home.tab.dashboard.DashboardTabViewModel] for the lifecycle contract
 * (Component is alive from app start; load is gated by `onAppeared` + pager visibility).
 */
class ProjectsTabViewModel(
  private val getProductionsUseCase: GetProductionsUseCase,
  dispatcher: CoroutineContext = Dispatchers.Main
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(ProjectsTabState())
  val state: StateFlow<ProjectsTabState> = _state.asStateFlow()

  private var hasLoaded = false
  private var refreshJob: Job? = null

  fun onAppeared() {
    if (hasLoaded) return
    hasLoaded = true
    scope.launch {
      _state.value = _state.value.copy(isLoading = true)
      load()
      _state.value = _state.value.copy(isLoading = false)
    }
  }

  fun onRefresh() {
    if (refreshJob?.isActive == true) return
    refreshJob = scope.launch {
      _state.value = _state.value.copy(isRefreshing = true)
      load()
      _state.value = _state.value.copy(isRefreshing = false)
    }
  }

  private suspend fun load() {
    when (val outcome = getProductionsUseCase()) {
      is Outcome.Success ->
        _state.value = _state.value.copy(productions = outcome.data.map { it.toUi() })
      is Outcome.Failure -> Unit
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
