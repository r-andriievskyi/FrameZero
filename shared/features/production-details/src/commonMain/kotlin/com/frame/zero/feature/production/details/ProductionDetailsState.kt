package com.frame.zero.feature.production.details

import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.ui.UiText

data class ProductionDetailsState(
  val isLoading: Boolean = false,
  val detail: ProductionDetail? = null,
  val error: UiText? = null,
  val tasks: List<ProductionTaskUi> = emptyList(),
  val areTasksLoading: Boolean = false,
  val isDeleteDialogVisible: Boolean = false,
  val isDeleting: Boolean = false,
  val deleteError: UiText? = null
)

/** A task row rendered in the tasks card, with display text resolved by the ViewModel. */
data class ProductionTaskUi(
  val id: String,
  val title: String,
  val dueDateLabel: String?,
  val isDone: Boolean
)
