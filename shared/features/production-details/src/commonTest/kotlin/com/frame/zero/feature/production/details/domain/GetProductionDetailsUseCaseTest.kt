package com.frame.zero.feature.production.details.domain

import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.feature.production.details.testing.FakeProductionsRepository
import com.frame.zero.feature.production.details.testing.productionDetailDto
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetProductionDetailsUseCaseTest {
  @Test
  fun `success maps dto to domain detail and forwards id`() =
    runTest {
      val repo = FakeProductionsRepository(detail = productionDetailDto(id = "p7", title = "Pilot"))

      val outcome = GetProductionDetailsUseCase(repo)(GetProductionDetailsUseCase.Params("p7"))

      val success = assertIs<Outcome.Success<ProductionDetail>>(outcome)
      assertEquals("p7", success.data.id)
      assertEquals("Pilot", success.data.title)
      assertEquals(listOf("p7"), repo.getIds)
    }

  @Test
  fun `IOException maps to Network failure`() =
    runTest {
      val repo = FakeProductionsRepository(getThrows = IOException("offline"))

      val outcome = GetProductionDetailsUseCase(repo)(GetProductionDetailsUseCase.Params("p7"))

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Network("offline"), failure.error)
    }
}
