package com.frame.zero.dto

import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.dto.production.CreateCrewMemberDto
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.ProductionSummaryDto
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class WireFormatContractTest {
  private val json = Json

  @Test
  fun `CreateProductionRequest serializes to the canonical wire shape`() {
    val request = CreateProductionRequest(
      title = "Pilot",
      genre = Genre.DRAMA,
      logline = "A taut thriller",
      startDate = LocalDate(2026, 4, 1),
      wrapDate = LocalDate(2026, 5, 1),
      budgetCents = 1_500_000,
      crew = listOf(CreateCrewMemberDto(name = "Ada", role = "Director", email = "ada@example.com"))
    )

    assertEquals(CREATE_PRODUCTION_REQUEST_JSON, json.encodeToString(request))
    assertEquals(request, json.decodeFromString<CreateProductionRequest>(CREATE_PRODUCTION_REQUEST_JSON))
  }

  @Test
  fun `ProductionSummaryDto serializes to the canonical wire shape`() {
    val summary = ProductionSummaryDto(
      id = "p1",
      title = "Pilot",
      genre = Genre.DRAMA,
      phase = ProductionPhase.PRODUCTION,
      progressPercent = 42,
      daysLeft = 7,
      membersCount = 3,
      updatedAt = Instant.parse("2026-04-01T10:30:00Z")
    )

    assertEquals(PRODUCTION_SUMMARY_JSON, json.encodeToString(summary))
    assertEquals(summary, json.decodeFromString<ProductionSummaryDto>(PRODUCTION_SUMMARY_JSON))
  }

  internal companion object {
    // Keep byte-identical to the copies in :server's WireFormatContractTest.
    const val CREATE_PRODUCTION_REQUEST_JSON: String =
      """{"title":"Pilot","genre":"DRAMA","logline":"A taut thriller","startDate":"2026-04-01",""" +
        """"wrapDate":"2026-05-01","budgetCents":1500000,""" +
        """"crew":[{"name":"Ada","role":"Director","email":"ada@example.com"}]}"""

    const val PRODUCTION_SUMMARY_JSON: String =
      """{"id":"p1","title":"Pilot","genre":"DRAMA","phase":"PRODUCTION","progressPercent":42,""" +
        """"daysLeft":7,"membersCount":3,"updatedAt":"2026-04-01T10:30:00Z"}"""
  }
}
