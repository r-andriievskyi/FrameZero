package com.frame.zero.domain.production

import kotlinx.datetime.LocalDate

/** Everything needed to create a production; the repository maps it to the wire request. */
data class NewProduction(
  val title: String,
  val genre: Genre,
  val logline: String? = null,
  val startDate: LocalDate,
  val wrapDate: LocalDate,
  val budgetCents: Long? = null,
  val crew: List<NewCrewMember> = emptyList()
)

data class NewCrewMember(
  val name: String,
  val role: String,
  val email: String? = null
)
