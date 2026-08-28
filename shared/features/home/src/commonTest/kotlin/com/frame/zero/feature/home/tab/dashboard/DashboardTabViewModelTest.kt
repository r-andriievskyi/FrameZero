package com.frame.zero.feature.home.tab.dashboard

import app.cash.turbine.test
import com.frame.zero.domain.User
import com.frame.zero.domain.dashboard.Dashboard
import com.frame.zero.domain.dashboard.DashboardStats
import com.frame.zero.domain.dashboard.DashboardTask
import com.frame.zero.domain.task.TaskStatus
import com.frame.zero.testing.FakeConnectivityObserver
import com.frame.zero.testing.FakeDashboardRepository
import com.frame.zero.testing.FakeUserRepository
import com.frame.zero.domain.OfflineException
import com.frame.zero.feature.home.usecase.GetDashboardUseCase
import com.frame.zero.feature.home.usecase.GetMeUseCase
import com.frame.zero.repository.dashboard.DashboardRepository
import com.frame.zero.repository.user.UserRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardTabViewModelTest {
  private val user = User(id = "u1", email = "u@x.com", firstName = "Ada", lastName = "Lovelace")
  private val dashboardResponse = Dashboard(
    displayName = "Ada",
    stats = DashboardStats(activeProjects = 2, openTasks = 5),
    myTasks = listOf(
      DashboardTask(
        id = "t1",
        title = "Storyboard",
        productionTitle = "Pilot",
        dueDate = null,
        status = TaskStatus.OPEN
      )
    )
  )

  @Test
  fun `init loads dashboard and user name then clears loading`() =
    runTest {
      val userRepo = FakeUserRepository(user = user)
      val dashboardRepo = FakeDashboardRepository(dashboard = dashboardResponse)
      val viewModel = makeViewModel(this, userRepo, dashboardRepo)

      viewModel.state.test {
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertFalse(state.isLoading)
        assertNull(state.error)
        val dashboard = assertNotNull(state.dashboard)
        assertEquals("Ada", dashboard.displayName)
        assertEquals(2, dashboard.stats.activeProjects)
        assertEquals(5, dashboard.stats.openTasks)
        assertEquals(1, dashboard.myTasks.size)
        assertEquals("Storyboard", dashboard.myTasks.single().title)
      }
      assertEquals(1, userRepo.getMeCalls)
      assertEquals(1, dashboardRepo.getDashboardCalls)
    }

  @Test
  fun `exposes a past due date with overdue urgency`() =
    runTest {
      val pastDue = dashboardResponse.copy(
        myTasks = listOf(
          DashboardTask(
            id = "t1",
            title = "Storyboard",
            productionTitle = "Pilot",
            dueDate = LocalDate(2020, 1, 15),
            status = TaskStatus.OPEN
          )
        )
      )
      val userRepo = FakeUserRepository(user = user)
      val dashboardRepo = FakeDashboardRepository(dashboard = pastDue)
      val viewModel = makeViewModel(this, userRepo, dashboardRepo)

      viewModel.state.test {
        advanceUntilIdle()

        val task = assertNotNull(expectMostRecentItem().dashboard).myTasks.single()
        assertEquals(LocalDate(2020, 1, 15), task.dueDate)
        assertEquals(DueUrgency.Overdue, task.dueUrgency)
      }
    }

  @Test
  fun `init sets loading true before completion`() =
    runTest {
      val dashGate = CompletableDeferred<Dashboard>()
      val dashboardRepo = GatedDashboardRepository(dashGate)
      val userRepo = FakeUserRepository(user = user)
      val viewModel = makeViewModel(this, userRepo, dashboardRepo)

      viewModel.state.test {
        runCurrent()

        assertTrue(expectMostRecentItem().isLoading)

        dashGate.complete(dashboardResponse)
        advanceUntilIdle()

        assertFalse(expectMostRecentItem().isLoading)
      }
    }

  @Test
  fun `getMe failure falls back to the first word of the dashboard displayName`() =
    runTest {
      val fullNameResponse = dashboardResponse.copy(displayName = "Ada Lovelace")
      val userRepo = FakeUserRepository(throws = RuntimeException("boom"))
      val dashboardRepo = FakeDashboardRepository(dashboard = fullNameResponse)
      val viewModel = makeViewModel(this, userRepo, dashboardRepo)

      viewModel.state.test {
        advanceUntilIdle()

        val state = expectMostRecentItem()
        val dashboard = assertNotNull(state.dashboard)
        assertEquals("Ada", dashboard.displayName)
        assertFalse(state.isLoading)
        assertNull(state.error)
      }
      assertEquals(1, dashboardRepo.getDashboardCalls)
    }

  @Test
  fun `non-network failure sets a Generic error and leaves dashboard null`() =
    runTest {
      val userRepo = FakeUserRepository(user = user)
      val dashboardRepo = FakeDashboardRepository(throws = RuntimeException("boom"))
      val viewModel = makeViewModel(this, userRepo, dashboardRepo)

      viewModel.state.test {
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertNull(state.dashboard)
        assertNotNull(state.error)
        assertFalse(state.error.autoRetries)
        assertFalse(state.isLoading)
      }
    }

  @Test
  fun `offline failure sets a Network error`() =
    runTest {
      val userRepo = FakeUserRepository(user = user)
      val dashboardRepo = FakeDashboardRepository(throws = OfflineException())
      val viewModel = makeViewModel(this, userRepo, dashboardRepo)

      viewModel.state.test {
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertNull(state.dashboard)
        assertNotNull(state.error)
        assertTrue(state.error.autoRetries)
      }
    }

  @Test
  fun `connection failure while online sets a Generic error`() =
    runTest {
      val userRepo = FakeUserRepository(user = user)
      val dashboardRepo = FakeDashboardRepository(throws = IOException("connection refused"))
      val viewModel = makeViewModel(this, userRepo, dashboardRepo)

      viewModel.state.test {
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertNull(state.dashboard)
        assertNotNull(state.error)
        assertFalse(state.error.autoRetries)
      }
    }

  @Test
  fun `reconnecting after a network failure auto-reloads`() =
    runTest {
      var shouldFail = true
      val dashboardRepo = object : DashboardRepository {
        var calls = 0

        override suspend fun getDashboard(): Dashboard {
          calls++
          if (shouldFail) throw OfflineException()
          return dashboardResponse
        }
      }
      val connectivity = FakeConnectivityObserver(initiallyOnline = false)
      val viewModel = makeViewModel(
        this,
        FakeUserRepository(user = user),
        dashboardRepo,
        connectivity
      )

      viewModel.state.test {
        advanceUntilIdle()
        val failed = expectMostRecentItem()
        assertNotNull(failed.error)
        assertTrue(failed.error.autoRetries)

        shouldFail = false
        connectivity.online.value = true
        advanceUntilIdle()

        val recovered = expectMostRecentItem()
        assertNull(recovered.error)
        assertNotNull(recovered.dashboard)
      }
      assertEquals(2, dashboardRepo.calls)
    }

  @Test
  fun `greeting uses only the first name and ignores the last name`() =
    runTest {
      val userRepo = FakeUserRepository(user = user.copy(firstName = "Ada", lastName = "Lovelace"))
      val dashboardRepo = FakeDashboardRepository(dashboard = dashboardResponse)
      val viewModel = makeViewModel(this, userRepo, dashboardRepo)

      viewModel.state.test {
        advanceUntilIdle()

        val dashboard = assertNotNull(expectMostRecentItem().dashboard)
        assertEquals("Ada", dashboard.displayName)
      }
    }

  @Test
  fun `retry reloads data after failure`() =
    runTest {
      var shouldFail = true
      val dashboardRepo = object : DashboardRepository {
        var calls = 0

        override suspend fun getDashboard(): Dashboard {
          calls++
          if (shouldFail) throw RuntimeException("boom")
          return dashboardResponse
        }
      }
      val userRepo = FakeUserRepository(user = user)
      val viewModel = makeViewModel(this, userRepo, dashboardRepo)

      viewModel.state.test {
        advanceUntilIdle()
        val failed = expectMostRecentItem()
        assertNotNull(failed.error)
        assertFalse(failed.error.autoRetries)

        shouldFail = false
        viewModel.onIntent(DashboardTabIntent.Retry)
        advanceUntilIdle()

        val recovered = expectMostRecentItem()
        assertNull(recovered.error)
        assertNotNull(recovered.dashboard)
      }
      assertEquals(2, dashboardRepo.calls)
    }

  @Test
  fun `onDestroy cancels scope so retry does not emit`() =
    runTest {
      val userRepo = FakeUserRepository(user = user)
      val dashboardRepo = FakeDashboardRepository(dashboard = dashboardResponse)
      val viewModel =
        DashboardTabViewModel(
          getMeUseCase = GetMeUseCase(userRepo),
          getDashboardUseCase = GetDashboardUseCase(dashboardRepo),
          connectivityObserver = FakeConnectivityObserver(),
          dispatcher = StandardTestDispatcher(testScheduler)
        )

      advanceUntilIdle()
      viewModel.onDestroy()

      // retry after destroy should be a no-op since the scope is cancelled
      viewModel.onIntent(DashboardTabIntent.Retry)
      advanceUntilIdle()

      // Initial load happened (1 call each), but retry didn't fire new calls
      assertEquals(1, userRepo.getMeCalls)
      assertEquals(1, dashboardRepo.getDashboardCalls)
    }

  private fun makeViewModel(
    scope: TestScope,
    userRepo: UserRepository = FakeUserRepository(user = user),
    dashboardRepo: DashboardRepository = FakeDashboardRepository(dashboard = dashboardResponse),
    connectivityObserver: FakeConnectivityObserver = FakeConnectivityObserver()
  ): DashboardTabViewModel =
    DashboardTabViewModel(
      getMeUseCase = GetMeUseCase(userRepo),
      getDashboardUseCase = GetDashboardUseCase(dashboardRepo),
      connectivityObserver = connectivityObserver,
      dispatcher = StandardTestDispatcher(scope.testScheduler)
    )

  private class GatedDashboardRepository(
    private val gate: CompletableDeferred<Dashboard>
  ) : DashboardRepository {
    override suspend fun getDashboard(): Dashboard = gate.await()
  }
}
