package com.frame.zero.feature.home.tab.projects

import com.frame.zero.domain.production.ProductionPhase

data class ProjectsTabState(
  val selectedFilter: ProductionPhase? = null
)
