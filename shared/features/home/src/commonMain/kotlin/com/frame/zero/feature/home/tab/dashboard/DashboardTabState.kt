package com.frame.zero.feature.home.tab.dashboard

import com.frame.zero.domain.dashboard.Dashboard

data class DashboardTabState(
  val isLoading: Boolean = false,
  val userName: String? = null,
  val dashboard: Dashboard? = null,
)
