package com.frame.zero.feature.home.tab.dashboard

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.Outcome
import com.frame.zero.feature.home.usecase.GetDashboardUseCase
import com.frame.zero.feature.home.usecase.GetMeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class DashboardTabViewModel(
  private val getMeUseCase: GetMeUseCase,
  private val getDashboardUseCase: GetDashboardUseCase,
  dispatcher: CoroutineContext = Dispatchers.Main
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(DashboardTabState())
  val state: StateFlow<DashboardTabState> = _state.asStateFlow()

  init {
    load()
  }

  fun retry() {
    load()
  }

  private fun load() {
    scope.launch {
      _state.update { DashboardTabState(isLoading = true) }
      val dashDeferred = async { getDashboardUseCase() }
      val meDeferred = async { getMeUseCase() }

      val dashResult = dashDeferred.await()
      val meResult = meDeferred.await()

      _state.update {
        when (dashResult) {
          is Outcome.Success -> {
            val userName = when (meResult) {
              is Outcome.Success -> {
                val user = meResult.data
                "${user.firstName} ${user.lastName}".trim()
              }
              is Outcome.Failure -> dashResult.data.displayName
            }
            DashboardTabState(
              isLoading = false,
              dashboard = dashResult.data.toUi().copy(displayName = userName)
            )
          }
          is Outcome.Failure -> DashboardTabState(isLoading = false, isError = true)
        }
      }
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
