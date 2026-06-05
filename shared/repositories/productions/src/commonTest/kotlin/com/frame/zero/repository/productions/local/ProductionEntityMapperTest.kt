package com.frame.zero.repository.productions.local

import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ProductionEntityMapperTest {
  @Test
  fun `toProduction parses enums and epoch millis`() {
    val entity = ProductionEntity(
      id = "p1",
      title = "Pilot",
      genre = "SCI_FI",
      phase = "POST_PRODUCTION",
      progressPercent = 80,
      daysLeft = 3,
      membersCount = 9,
      updatedAtEpochMs = 1_700_000_000_000,
      pageOrder = 0
    )

    val production = entity.toProduction()

    assertEquals("p1", production.id)
    assertEquals("Pilot", production.title)
    assertEquals(Genre.SCI_FI, production.genre)
    assertEquals(ProductionPhase.POST_PRODUCTION, production.phase)
    assertEquals(80, production.progressPercent)
    assertEquals(3, production.daysLeft)
    assertEquals(9, production.membersCount)
    assertEquals(Instant.fromEpochMilliseconds(1_700_000_000_000), production.updatedAt)
  }
}
