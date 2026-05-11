package com.frame.zero.feature.production.details

sealed interface ProductionDetailsIntent {
  data object Refresh : ProductionDetailsIntent
}
