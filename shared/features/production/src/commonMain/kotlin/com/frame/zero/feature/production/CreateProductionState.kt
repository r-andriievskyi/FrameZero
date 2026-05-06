package com.frame.zero.feature.production

import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import kotlinx.datetime.LocalDate

data class CreateProductionState(
  val title: String = "",
  val genre: Genre = Genre.DRAMA,
  val phase: ProductionPhase = ProductionPhase.PRE_PRODUCTION,
  val logline: String = "",
  val startDate: LocalDate? = null,
  val wrapDate: LocalDate? = null,
  val isLoading: Boolean = false,
  val error: String? = null,
  val isSuccess: Boolean = false
)
