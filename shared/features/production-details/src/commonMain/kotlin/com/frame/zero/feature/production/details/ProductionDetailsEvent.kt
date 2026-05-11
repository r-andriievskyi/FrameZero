package com.frame.zero.feature.production.details

sealed interface ProductionDetailsEvent {
  data class Deleted(
    val productionId: String
  ) : ProductionDetailsEvent
}
