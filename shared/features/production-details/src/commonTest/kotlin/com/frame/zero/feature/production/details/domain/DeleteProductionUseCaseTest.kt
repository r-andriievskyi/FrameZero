package com.frame.zero.feature.production.details.domain

import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.feature.production.details.testing.FakeProductionsRepository
import com.frame.zero.feature.production.details.testing.responseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DeleteProductionUseCaseTest {
  @Test
  fun `success deletes and forwards id`() =
    runTest {
      val repo = FakeProductionsRepository()

      val outcome = DeleteProductionUseCase(repo)(DeleteProductionUseCase.Params("p1"))

      assertIs<Outcome.Success<Unit>>(outcome)
      assertEquals(listOf("p1"), repo.deletedIds)
    }

  @Test
  fun `forbidden maps to Forbidden`() =
    runTest {
      val repo = FakeProductionsRepository(deleteThrows = responseException(HttpStatusCode.Forbidden))

      val outcome = DeleteProductionUseCase(repo)(DeleteProductionUseCase.Params("p1"))

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Forbidden, failure.error)
    }

  @Test
  fun `not found maps to NotFound`() =
    runTest {
      val repo = FakeProductionsRepository(deleteThrows = responseException(HttpStatusCode.NotFound))

      val outcome = DeleteProductionUseCase(repo)(DeleteProductionUseCase.Params("p1"))

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.NotFound, failure.error)
    }

  @Test
  fun `conflict maps to Conflict`() =
    runTest {
      val repo = FakeProductionsRepository(deleteThrows = responseException(HttpStatusCode.Conflict))

      val outcome = DeleteProductionUseCase(repo)(DeleteProductionUseCase.Params("p1"))

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Conflict, failure.error)
    }

  @Test
  fun `server error maps to Server`() =
    runTest {
      val repo =
        FakeProductionsRepository(deleteThrows = responseException(HttpStatusCode.InternalServerError))

      val outcome = DeleteProductionUseCase(repo)(DeleteProductionUseCase.Params("p1"))

      val failure = assertIs<Outcome.Failure>(outcome)
      assertIs<DomainError.Server>(failure.error)
    }

  @Test
  fun `IOException maps to Network failure`() =
    runTest {
      val repo = FakeProductionsRepository(deleteThrows = IOException("offline"))

      val outcome = DeleteProductionUseCase(repo)(DeleteProductionUseCase.Params("p1"))

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Network("offline"), failure.error)
    }
}
