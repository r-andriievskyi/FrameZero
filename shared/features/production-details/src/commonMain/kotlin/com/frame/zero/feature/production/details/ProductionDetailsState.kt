package com.frame.zero.feature.production.details

import com.frame.zero.domain.production.ProductionDetail

data class ProductionDetailsState(
  val isLoading: Boolean = false,
  val detail: ProductionDetail? = null,
  val error: String? = null
)
