package com.frame.zero.feature.production.details

sealed interface ProductionDetailsIntent {
  data object Refresh : ProductionDetailsIntent

  data object RefreshTasks : ProductionDetailsIntent

  data object AddTaskRequested : ProductionDetailsIntent

  data object DeleteRequested : ProductionDetailsIntent

  data object DeleteConfirmed : ProductionDetailsIntent

  data object DeleteDismissed : ProductionDetailsIntent

  data object DeleteErrorDismissed : ProductionDetailsIntent
}
