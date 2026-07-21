package com.frame.zero.feature.appupdate

import com.frame.zero.core.appupdate.StoreLauncher
import com.frame.zero.domain.Outcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppUpdateController(
  private val checkAppUpdate: CheckAppUpdateUseCase,
  private val storeLauncher: StoreLauncher
) {
  private val _state = MutableStateFlow<AppUpdateState>(AppUpdateState.None)
  val state: StateFlow<AppUpdateState> = _state.asStateFlow()

  private val softDismissed = MutableStateFlow(false)

  suspend fun refresh() {
    val resolved = when (val outcome = checkAppUpdate()) {
      is Outcome.Success -> outcome.data
      is Outcome.Failure -> AppUpdateState.None
    }
    _state.update { gate(resolved) }
  }

  fun dismissSoft() {
    softDismissed.value = true
    _state.update { gate(it) }
  }

  fun openStore() {
    _state.value.activeStoreUrl?.takeIf { it.isNotBlank() }?.let(storeLauncher::open)
  }

  private fun gate(state: AppUpdateState): AppUpdateState =
    if (state is AppUpdateState.Soft && softDismissed.value) AppUpdateState.None else state
}
