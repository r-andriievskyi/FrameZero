package com.frame.zero.feature.home.usecase

import com.frame.zero.domain.OfflineException
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.dashboard.Dashboard
import com.frame.zero.domain.dashboard.DashboardStats
import com.frame.zero.testing.FakeDashboardRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetDashboardUseCaseTest {
  private val dashboard = Dashboard(
    displayName = "Ada",
    stats = DashboardStats(activeProjects = 2, openTasks = 5),
    myTasks = emptyList()
  )

  @Test
  fun `success returns the domain dashboard`() =
    runTest {
      val repo = FakeDashboardRepository(dashboard = dashboard)

      val outcome = GetDashboardUseCase(repo)()

      val success = assertIs<Outcome.Success<Dashboard>>(outcome)
      assertEquals("Ada", success.data.displayName)
      assertEquals(2, success.data.stats.activeProjects)
      assertEquals(1, repo.getDashboardCalls)
    }

  @Test
  fun `OfflineException maps to Offline failure`() =
    runTest {
      val repo = FakeDashboardRepository(throws = OfflineException("offline"))

      val outcome = GetDashboardUseCase(repo)()

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Offline("offline"), failure.error)
    }

  @Test
  fun `generic exception maps to Unknown failure`() =
    runTest {
      val repo = FakeDashboardRepository(throws = RuntimeException("boom"))

      val outcome = GetDashboardUseCase(repo)()

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Unknown("boom"), failure.error)
    }
}
