package com.frame.zero.feature.appupdate

import com.frame.zero.core.appupdate.StoreLauncher
import com.frame.zero.core.network.connectivity.ConnectivityObserver
import com.frame.zero.domain.Outcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns what the root gate shows. The published [state] is re-derived by [publish] from three inputs
 * whenever any changes: the last resolved [AppUpdateState], whether the soft prompt was dismissed,
 * and the metered state. All three are [MutableStateFlow]s and [state] is updated through an atomic
 * `update`, so there is no plain mutable field to race on — a write from the metered collector and a
 * concurrent `refresh` cannot tear the published value.
 *
 * Presentation timing lives here, not in [CheckAppUpdateUseCase]:
 * - A **hard** gate always shows — the build is below minimum and the API is unreachable anyway.
 * - A **soft**, non-`critical` prompt is deferred while the connection is metered (cellular),
 *   resurfacing when the device returns to an unmetered network (observed live) or on the next
 *   refresh. A `critical` soft prompt is never deferred.
 * - A dismissed soft prompt stays hidden for the process (resurfaces next cold start).
 *
 * [scope] defaults to an app-lifetime scope (this is a Koin `single`); tests inject their own.
 */
class AppUpdateController(
  private val checkAppUpdate: CheckAppUpdateUseCase,
  private val storeLauncher: StoreLauncher,
  connectivity: ConnectivityObserver,
  scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
  private val resolved = MutableStateFlow<AppUpdateState>(AppUpdateState.None)
  private val softDismissed = MutableStateFlow(false)
  private val metered = MutableStateFlow(connectivity.isCurrentlyMetered())

  private val _state = MutableStateFlow<AppUpdateState>(AppUpdateState.None)
  val state: StateFlow<AppUpdateState> = _state.asStateFlow()

  init {
    scope.launch {
      connectivity.isMetered.collect { isMetered ->
        metered.value = isMetered
        publish()
      }
    }
  }

  suspend fun refresh() {
    resolved.value = when (val outcome = checkAppUpdate()) {
      is Outcome.Success -> outcome.data
      is Outcome.Failure -> AppUpdateState.None
    }
    publish()
  }

  fun dismissSoft() {
    softDismissed.value = true
    publish()
  }

  fun openStore() {
    _state.value.activeStoreUrl?.takeIf { it.isNotBlank() }?.let(storeLauncher::open)
  }

  private fun publish() {
    val gated = gate(resolved.value, softDismissed.value, metered.value)
    _state.update { gated }
  }

  private fun gate(
    state: AppUpdateState,
    dismissed: Boolean,
    metered: Boolean
  ): AppUpdateState =
    when (state) {
      is AppUpdateState.Soft if dismissed -> AppUpdateState.None
      is AppUpdateState.Soft if !state.critical && metered -> AppUpdateState.None
      else -> state
    }
}
