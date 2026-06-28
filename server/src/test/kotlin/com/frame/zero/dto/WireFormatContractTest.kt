package com.frame.zero.dto

import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.ProductionSummaryDto
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class WireFormatContractTest {
  private val json = Json

  @Test
  fun `server parses and reproduces the client CreateProductionRequest wire shape`() {
    val parsed = json.decodeFromString<CreateProductionRequest>(CREATE_PRODUCTION_REQUEST_JSON)

    assertEquals("Pilot", parsed.title)
    assertEquals(Genre.DRAMA, parsed.genre)
    assertEquals(LocalDate(2026, 4, 1), parsed.startDate)
    assertEquals(1_500_000, parsed.budgetCents)
    assertEquals("Ada", parsed.crew.single().name)
    // Re-serializing must produce the identical bytes the client asserts on.
    assertEquals(CREATE_PRODUCTION_REQUEST_JSON, json.encodeToString(parsed))
  }

  @Test
  fun `server parses and reproduces the client ProductionSummaryDto wire shape`() {
    val parsed = json.decodeFromString<ProductionSummaryDto>(PRODUCTION_SUMMARY_JSON)

    assertEquals("p1", parsed.id)
    assertEquals(ProductionPhase.PRODUCTION, parsed.phase)
    assertEquals(Instant.parse("2026-04-01T10:30:00Z"), parsed.updatedAt)
    assertEquals(PRODUCTION_SUMMARY_JSON, json.encodeToString(parsed))
  }

  private companion object {
    // Keep byte-identical to the copies in :shared's WireFormatContractTest.
    const val CREATE_PRODUCTION_REQUEST_JSON: String =
      """{"title":"Pilot","genre":"DRAMA","logline":"A taut thriller","startDate":"2026-04-01",""" +
        """"wrapDate":"2026-05-01","budgetCents":1500000,""" +
        """"crew":[{"name":"Ada","role":"Director","email":"ada@example.com"}]}"""

    const val PRODUCTION_SUMMARY_JSON: String =
      """{"id":"p1","title":"Pilot","genre":"DRAMA","phase":"PRODUCTION","progressPercent":42,""" +
        """"daysLeft":7,"membersCount":3,"updatedAt":"2026-04-01T10:30:00Z"}"""
  }
}
