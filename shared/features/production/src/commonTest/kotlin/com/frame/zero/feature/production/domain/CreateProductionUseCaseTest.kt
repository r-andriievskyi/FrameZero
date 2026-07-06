package com.frame.zero.feature.production.domain

import com.frame.zero.domain.OfflineException
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.NewCrewMember
import com.frame.zero.testing.FakeProductionsRepository
import com.frame.zero.testing.productionDetail
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CreateProductionUseCaseTest {
  private fun params(
    title: String = "Pilot",
    logline: String? = "A story",
    crew: List<NewCrewMember> = emptyList()
  ): CreateProductionUseCase.Params =
    CreateProductionUseCase.Params(
      title = title,
      genre = Genre.DRAMA,
      logline = logline,
      startDate = LocalDate(2026, 4, 1),
      wrapDate = LocalDate(2026, 5, 1),
      budgetCents = 123_456,
      crew = crew
    )

  @Test
  fun `success builds request from params and returns domain production`() =
    runTest {
      val repo = FakeProductionsRepository(detail = productionDetail(id = "p9", title = "Pilot"))
      val crew = listOf(NewCrewMember(name = "Ada", role = "Director"))

      val outcome = CreateProductionUseCase(repo)(params(crew = crew))

      val success = assertIs<Outcome.Success<Production>>(outcome)
      assertEquals("p9", success.data.id)
      val request = repo.createRequests.single()
      assertEquals("Pilot", request.title)
      assertEquals(Genre.DRAMA, request.genre)
      assertEquals("A story", request.logline)
      assertEquals(LocalDate(2026, 4, 1), request.startDate)
      assertEquals(LocalDate(2026, 5, 1), request.wrapDate)
      assertEquals(123_456, request.budgetCents)
      assertEquals(crew, request.crew)
    }

  @Test
  fun `blank logline becomes null in request`() =
    runTest {
      val repo = FakeProductionsRepository()

      CreateProductionUseCase(repo)(params(logline = "   "))

      assertEquals(null, repo.createRequests.single().logline)
    }

  @Test
  fun `OfflineException maps to Network failure`() =
    runTest {
      val repo = FakeProductionsRepository(createThrows = OfflineException("offline"))

      val outcome = CreateProductionUseCase(repo)(params())

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Offline("offline"), failure.error)
    }

  @Test
  fun `IOException maps to Server failure server unreachable while online`() =
    runTest {
      val repo = FakeProductionsRepository(createThrows = IOException("connection refused"))

      val outcome = CreateProductionUseCase(repo)(params())

      val failure = assertIs<Outcome.Failure>(outcome)
      val server = assertIs<DomainError.Server>(failure.error)
      assertEquals("connection refused", server.message)
    }
}
