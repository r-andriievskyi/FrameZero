package com.frame.zero.feature.home.tab.dashboard

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.Outcome
import com.frame.zero.feature.home.usecase.GetDashboardUseCase
import com.frame.zero.feature.home.usecase.GetMeUseCase
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardTabViewModel(
  private val getMeUseCase: GetMeUseCase,
  private val getDashboardUseCase: GetDashboardUseCase,
  dispatcher: CoroutineContext = Dispatchers.Main,
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(DashboardTabState())
  val state: StateFlow<DashboardTabState> = _state.asStateFlow()

  private var hasLoaded = false

  fun onAppeared() {
    if (hasLoaded) return
    hasLoaded = true
    scope.launch {
      _state.value = _state.value.copy(isLoading = true)
      when (val outcome = getMeUseCase()) {
        is Outcome.Success -> {
          val user = outcome.data
          _state.value = _state.value.copy(userName = "${user.firstName} ${user.lastName}".trim())
        }
        is Outcome.Failure -> Unit
      }
      when (val outcome = getDashboardUseCase()) {
        is Outcome.Success ->
          _state.value = _state.value.copy(isLoading = false, dashboard = outcome.data)
        is Outcome.Failure -> _state.value = _state.value.copy(isLoading = false)
      }
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
