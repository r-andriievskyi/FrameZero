package com.frame.zero.feature.production

import com.frame.zero.domain.production.Genre
import com.frame.zero.ui.UiText
import kotlinx.datetime.LocalDate

data class CrewMemberEntry(
  val name: String,
  val role: String
) {
  /** First and last initial ("Dara Okonkwo" → "DO"), one for a single name, "?" for a blank one. */
  val initials: String
    get() {
      val parts = name.trim().split(WHITESPACE).filter { it.isNotEmpty() }
      return when {
        parts.size >= 2 -> "${parts.first().first().uppercaseChar()}${parts.last().first().uppercaseChar()}"
        parts.size == 1 -> parts[0].first().uppercaseChar().toString()
        else -> "?"
      }
    }

  private companion object {
    val WHITESPACE = Regex("\\s+")
  }
}

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
  val error: UiText? = null,
  val errorToast: UiText? = null
)
