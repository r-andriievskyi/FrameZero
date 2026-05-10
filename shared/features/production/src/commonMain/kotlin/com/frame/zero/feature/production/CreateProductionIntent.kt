package com.frame.zero.feature.production

import com.frame.zero.domain.production.Genre
import kotlinx.datetime.LocalDate

sealed interface CreateProductionIntent {
  data class TitleChanged(
    val title: String
  ) : CreateProductionIntent

  data class GenreChanged(
    val genre: Genre
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

  data class BudgetChanged(
    val budgetCents: Long?
  ) : CreateProductionIntent

  data class CrewNameChanged(
    val name: String
  ) : CreateProductionIntent

  data class CrewRoleChanged(
    val role: String
  ) : CreateProductionIntent

  data object AddCrewMember : CreateProductionIntent

  data class RemoveCrewMember(
    val index: Int
  ) : CreateProductionIntent

  data object NextStep : CreateProductionIntent

  data object PreviousStep : CreateProductionIntent

  data object Submit : CreateProductionIntent
}
