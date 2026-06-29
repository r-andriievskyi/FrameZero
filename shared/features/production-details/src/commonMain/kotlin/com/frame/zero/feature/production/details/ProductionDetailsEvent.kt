package com.frame.zero.feature.production.details

sealed interface ProductionDetailsEvent {
  data class Deleted(
    val productionId: String
  ) : ProductionDetailsEvent

  data class AddTaskRequested(
    val productionId: String,
    val productionTitle: String
  ) : ProductionDetailsEvent
}
