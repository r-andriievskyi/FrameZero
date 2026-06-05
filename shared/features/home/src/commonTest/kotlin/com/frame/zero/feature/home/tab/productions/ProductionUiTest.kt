package com.frame.zero.feature.home.tab.productions

import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.ProductionPhase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ProductionUiTest {
  @Test
  fun `toUi copies every displayed field`() {
    val production = Production(
      id = "p1",
      title = "Pilot",
      genre = Genre.DRAMA,
      phase = ProductionPhase.PRODUCTION,
      progressPercent = 42,
      daysLeft = 7,
      membersCount = 5,
      updatedAt = Instant.fromEpochMilliseconds(1_700_000_000_000)
    )

    val ui = production.toUi()

    assertEquals(
      ProductionUi(
        id = "p1",
        title = "Pilot",
        genre = Genre.DRAMA,
        phase = ProductionPhase.PRODUCTION,
        progressPercent = 42,
        daysLeft = 7,
        membersCount = 5
      ),
      ui
    )
  }
}
