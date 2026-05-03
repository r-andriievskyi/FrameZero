package com.frame.zero.feature.home.tab.dashboard

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * State holder for the Dashboard tab.
 *
 * The VM survives in [DashboardTabComponent]'s instanceKeeper for the full lifetime of the
 * enclosing Home component, so its state is preserved across tab swipes.
 *
 * Loading is intentionally NOT in `init`. Pager-driven preloading (see
 * [com.frame.zero.feature.home.HomeComponent]) means every tab Component is alive from app start —
 * if init ran the load, every tab would fetch on launch regardless of pager visibility. Instead the
 * UI calls [onAppeared] when this tab's page first composes, gated by the pager's
 * `beyondViewportPageCount`.
 */
class DashboardTabViewModel(dispatcher: CoroutineContext = Dispatchers.Main) :
  InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(DashboardTabState())
  val state: StateFlow<DashboardTabState> = _state.asStateFlow()

  private var hasLoaded = false

  /** One-shot. Subsequent calls (e.g. recomposition) are no-ops. */
  fun onAppeared() {
    if (hasLoaded) return
    hasLoaded = true
    // Initial data load goes here. Wire use cases via ctor params + featureHomeModule when ready.
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
