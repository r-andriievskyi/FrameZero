package com.frame.zero.feature.home.tab.projects

data class ProjectsTabState(
  val isLoading: Boolean = false,
  val isRefreshing: Boolean = false,
  val productions: List<ProductionUi> = emptyList()
)
