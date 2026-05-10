package com.frame.zero.feature.production

import com.frame.zero.domain.production.Genre
import kotlinx.datetime.LocalDate

data class CrewMemberEntry(
  val name: String,
  val role: String
)

data class CreateProductionState(
  val currentStep: Int = 1,
  val title: String = "",
  val genre: Genre = Genre.DRAMA,
  val logline: String = "",
  val startDate: LocalDate? = null,
  val wrapDate: LocalDate? = null,
  val budgetCents: Long? = null,
  val crewNameInput: String = "",
  val crewRoleInput: String = "Director",
  val crewMembers: List<CrewMemberEntry> = emptyList(),
  val isLoading: Boolean = false,
  val error: String? = null,
  val isSuccess: Boolean = false
) {
  val totalSteps: Int get() = 3

  val canAdvanceStep1: Boolean
    get() = title.isNotBlank() &&
      startDate != null && wrapDate != null &&
      wrapDate.toEpochDays() > startDate.toEpochDays()
}
