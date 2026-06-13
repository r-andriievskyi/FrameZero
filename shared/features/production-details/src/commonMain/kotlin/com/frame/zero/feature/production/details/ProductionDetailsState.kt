package com.frame.zero.feature.production.details

import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.ui.UiText

data class ProductionDetailsState(
  val isLoading: Boolean = false,
  val detail: ProductionDetail? = null,
  val error: UiText? = null,
  val isDeleteDialogVisible: Boolean = false,
  val isDeleting: Boolean = false,
  val deleteError: UiText? = null
)
