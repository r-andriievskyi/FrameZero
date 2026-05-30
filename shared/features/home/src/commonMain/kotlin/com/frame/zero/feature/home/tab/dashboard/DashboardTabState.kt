package com.frame.zero.feature.home.tab.dashboard

data class DashboardTabState(
  val isLoading: Boolean = false,
  val dashboard: DashboardUi? = null,
  val isError: Boolean = false
)
