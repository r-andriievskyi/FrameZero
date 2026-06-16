package com.frame.zero.feature.production.details

sealed interface ProductionDetailsIntent {
  data object Refresh : ProductionDetailsIntent

  /** Reload just the tasks list — emitted when the screen resumes (e.g. after creating a task). */
  data object RefreshTasks : ProductionDetailsIntent

  data object DeleteRequested : ProductionDetailsIntent

  data object DeleteConfirmed : ProductionDetailsIntent

  data object DeleteDismissed : ProductionDetailsIntent

  data object DeleteErrorDismissed : ProductionDetailsIntent
}
