package com.frame.zero.feature.home.tab.projects

import com.frame.zero.domain.production.Production

data class ProjectsTabState(
  val isLoading: Boolean = false,
  val productions: List<Production> = emptyList(),
)
