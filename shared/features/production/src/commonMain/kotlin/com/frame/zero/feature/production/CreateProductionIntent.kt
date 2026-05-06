package com.frame.zero.feature.production

import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import kotlinx.datetime.LocalDate

sealed interface CreateProductionIntent {
  data class TitleChanged(
    val title: String
  ) : CreateProductionIntent

  data class GenreChanged(
    val genre: Genre
  ) : CreateProductionIntent

  data class PhaseChanged(
    val phase: ProductionPhase
  ) : CreateProductionIntent

  data class LoglineChanged(
    val logline: String
  ) : CreateProductionIntent

  data class StartDateChanged(
    val date: LocalDate
  ) : CreateProductionIntent

  data class WrapDateChanged(
    val date: LocalDate
  ) : CreateProductionIntent

  data object Submit : CreateProductionIntent
}
