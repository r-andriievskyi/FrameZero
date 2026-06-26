package com.frame.zero.feature.production.details

import androidx.compose.runtime.Immutable
import com.frame.zero.ui.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ProductionDetailsState(
  val isLoading: Boolean = false,
  val detail: ProductionDetailUi? = null,
  val error: UiText? = null,
  val tasks: ImmutableList<ProductionTaskUi> = persistentListOf(),
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
