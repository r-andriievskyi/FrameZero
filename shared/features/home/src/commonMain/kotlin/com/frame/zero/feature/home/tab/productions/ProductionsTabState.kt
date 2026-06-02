package com.frame.zero.feature.home.tab.productions

import com.frame.zero.domain.production.ProductionPhase

data class ProductionsTabState(
  val selectedFilter: ProductionFilter = ProductionFilter.All,
  val availableFilters: List<ProductionFilter> = buildList {
    add(ProductionFilter.All)
    ProductionPhase.entries.forEach { add(ProductionFilter.ByPhase(it)) }
  }
)
