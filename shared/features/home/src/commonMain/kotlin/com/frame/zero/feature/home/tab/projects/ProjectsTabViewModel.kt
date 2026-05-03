package com.frame.zero.feature.home.tab.projects

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
 * See [com.frame.zero.feature.home.tab.dashboard.DashboardTabViewModel] for the lifecycle contract
 * (Component is alive from app start; load is gated by `onAppeared` + pager visibility).
 */
class ProjectsTabViewModel(dispatcher: CoroutineContext = Dispatchers.Main) :
  InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(ProjectsTabState())
  val state: StateFlow<ProjectsTabState> = _state.asStateFlow()

  private var hasLoaded = false

  fun onAppeared() {
    if (hasLoaded) return
    hasLoaded = true
    // Initial data load goes here.
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
