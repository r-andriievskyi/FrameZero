package com.frame.zero.feature.home.tab.dashboard

import com.frame.zero.ui.UiText

data class DashboardTabState(
  val isLoading: Boolean = false,
  val dashboard: DashboardUi? = null,
  val error: UiText? = null,
  /** True when [error] is the auto-retry-on-reconnect case — the UI hides the retry button. */
  val isOffline: Boolean = false
)
