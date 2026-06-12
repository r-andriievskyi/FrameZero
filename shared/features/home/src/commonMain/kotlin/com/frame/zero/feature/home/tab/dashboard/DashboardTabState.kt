package com.frame.zero.feature.home.tab.dashboard

import com.frame.zero.feature.home.LoadErrorKind

data class DashboardTabState(
  val isLoading: Boolean = false,
  val dashboard: DashboardUi? = null,
  val error: LoadErrorKind? = null
)
