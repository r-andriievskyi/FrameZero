package com.frame.zero.feature.home.tab.dashboard

sealed interface DashboardTabIntent {
  data object Retry : DashboardTabIntent
}
