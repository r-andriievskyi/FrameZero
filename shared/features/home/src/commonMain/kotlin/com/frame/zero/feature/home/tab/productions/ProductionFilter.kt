package com.frame.zero.feature.home.tab.productions

import com.frame.zero.domain.production.ProductionPhase

sealed interface ProductionFilter {
  data object All : ProductionFilter
  data class ByPhase(val phase: ProductionPhase) : ProductionFilter
}

fun ProductionFilter.toPhaseOrNull(): ProductionPhase? =
  when (this) {
    ProductionFilter.All -> null
    is ProductionFilter.ByPhase -> phase
  }


