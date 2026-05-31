package com.frame.zero.feature.production

import com.frame.zero.domain.production.Genre
import kotlinx.datetime.LocalDate

data class CrewMemberEntry(
  val name: String,
  val role: String
)

data class CreateProductionState(
  val currentStep: Int = 1,
  val totalSteps: Int = 3,
  val title: String = "",
  val genre: Genre = Genre.DRAMA,
  val logline: String = "",
  val startDate: LocalDate? = null,
  val wrapDate: LocalDate? = null,
  val budgetCents: Long? = null,
  val budgetDisplay: String? = null,
  val crewNameInput: String = "",
  val crewRoleInput: String = DEFAULT_CREW_ROLE,
  val crewMembers: List<CrewMemberEntry> = emptyList(),
  val canAdvanceStep1: Boolean = false,
  val isLoading: Boolean = false,
  val error: String? = null
)
