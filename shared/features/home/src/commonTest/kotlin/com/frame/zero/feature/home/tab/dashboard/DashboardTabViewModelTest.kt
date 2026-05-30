package com.frame.zero.feature.home.tab.dashboard

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.dto.dashboard.DashboardResponse
import com.frame.zero.dto.dashboard.GreetingDto
import com.frame.zero.dto.dashboard.StatsDto
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.dto.task.TaskSummaryDto
import com.frame.zero.feature.home.testing.FakeDashboardRepository
import com.frame.zero.feature.home.testing.FakeUserRepository
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardTabViewModelTest {
  private val userDto = UserDto(id = "u1", email = "u@x.com", firstName = "Ada", lastName = "Lovelace")
  private val dashboardResponse = DashboardResponse(
    greeting = GreetingDto(displayName = "Ada", activeProductionsCount = 2, openTasksCount = 5),
    stats = StatsDto(activeProjects = 2, openTasks = 5),
    myTasks = listOf(
      TaskSummaryDto(
        id = "t1",
        title = "Storyboard",
        productionTitle = "Pilot",
        dueDate = null,
        dueLabel = "Tomorrow",
        status = TaskStatus.OPEN
      )
    )
  )

  @Test
  fun `init loads dashboard and user name then clears loading`() =
    runTest {
      val userRepo = FakeUserRepository(userDto = userDto)
      val dashboardRepo = FakeDashboardRepository(response = dashboardResponse)
      val viewModel = makeViewModel(this, userRepo, dashboardRepo)

      advanceUntilIdle()

      assertFalse(viewModel.state.value.isLoading)
      assertFalse(viewModel.state.value.isError)
      val dashboard = assertNotNull(viewModel.state.value.dashboard)
      assertEquals("Ada Lovelace", dashboard.displayName)
      assertEquals(2, dashboard.stats.activeProjects)
      assertEquals(5, dashboard.stats.openTasks)
      assertEquals(1, dashboard.myTasks.size)
      assertEquals("Storyboard", dashboard.myTasks.single().title)
      assertEquals(1, userRepo.getMeCalls)
      assertEquals(1, dashboardRepo.getDashboardCalls)
    }

  @Test
  fun `init sets loading true before completion`() =
    runTest {
      val dashGate = CompletableDeferred<DashboardResponse>()
      val dashboardRepo = GatedDashboardRepository(dashGate)
      val userRepo = FakeUserRepository(userDto = userDto)
      val viewModel = makeViewModel(this, userRepo, dashboardRepo)

      runCurrent()

      assertTrue(viewModel.state.value.isLoading)

      dashGate.complete(dashboardResponse)
      advanceUntilIdle()

      assertFalse(viewModel.state.value.isLoading)
    }

  @Test
  fun `getMe failure uses dashboard displayName as fallback`() =
    runTest {
      val userRepo = FakeUserRepository(throws = RuntimeException("boom"))
      val dashboardRepo = FakeDashboardRepository(response = dashboardResponse)
      val viewModel = makeViewModel(this, userRepo, dashboardRepo)

      advanceUntilIdle()

      val dashboard = assertNotNull(viewModel.state.value.dashboard)
      assertEquals("Ada", dashboard.displayName)
      assertFalse(viewModel.state.value.isLoading)
      assertFalse(viewModel.state.value.isError)
      assertEquals(1, dashboardRepo.getDashboardCalls)
    }

  @Test
  fun `getDashboard failure sets isError true and leaves dashboard null`() =
    runTest {
      val userRepo = FakeUserRepository(userDto = userDto)
      val dashboardRepo = FakeDashboardRepository(throws = RuntimeException("boom"))
      val viewModel = makeViewModel(this, userRepo, dashboardRepo)

      advanceUntilIdle()

      assertNull(viewModel.state.value.dashboard)
      assertTrue(viewModel.state.value.isError)
      assertFalse(viewModel.state.value.isLoading)
    }

  @Test
  fun `getMe trims surrounding whitespace when last name is blank`() =
    runTest {
      val userRepo = FakeUserRepository(userDto = userDto.copy(firstName = "Ada", lastName = ""))
      val dashboardRepo = FakeDashboardRepository(response = dashboardResponse)
      val viewModel = makeViewModel(this, userRepo, dashboardRepo)

      advanceUntilIdle()

      val dashboard = assertNotNull(viewModel.state.value.dashboard)
      assertEquals("Ada", dashboard.displayName)
    }

  @Test
  fun `retry reloads data after failure`() =
    runTest {
      var shouldFail = true
      val dashboardRepo = object : DashboardRepository {
        var calls = 0

        override suspend fun getDashboard(): DashboardResponse {
          calls++
          if (shouldFail) throw RuntimeException("boom")
          return dashboardResponse
        }
      }
      val userRepo = FakeUserRepository(userDto = userDto)
      val viewModel = makeViewModel(this, userRepo, dashboardRepo)

      advanceUntilIdle()
      assertTrue(viewModel.state.value.isError)

      shouldFail = false
      viewModel.retry()
      advanceUntilIdle()

      assertFalse(viewModel.state.value.isError)
      assertNotNull(viewModel.state.value.dashboard)
      assertEquals(2, dashboardRepo.calls)
    }

  @Test
  fun `onDestroy cancels scope so retry does not emit`() =
    runTest {
      val userRepo = FakeUserRepository(userDto = userDto)
      val dashboardRepo = FakeDashboardRepository(response = dashboardResponse)
      val viewModel =
        DashboardTabViewModel(
          getMeUseCase = GetMeUseCase(userRepo),
          getDashboardUseCase = GetDashboardUseCase(dashboardRepo),
          dispatcher = StandardTestDispatcher(testScheduler)
        )

      advanceUntilIdle()
      viewModel.onDestroy()

      // retry after destroy should be a no-op since the scope is cancelled
      viewModel.retry()
      advanceUntilIdle()

      // Initial load happened (1 call each), but retry didn't fire new calls
      assertEquals(1, userRepo.getMeCalls)
      assertEquals(1, dashboardRepo.getDashboardCalls)
    }

  private fun makeViewModel(
    scope: TestScope,
    userRepo: UserRepository = FakeUserRepository(userDto = userDto),
    dashboardRepo: DashboardRepository = FakeDashboardRepository(response = dashboardResponse)
  ): DashboardTabViewModel =
    DashboardTabViewModel(
      getMeUseCase = GetMeUseCase(userRepo),
      getDashboardUseCase = GetDashboardUseCase(dashboardRepo),
      dispatcher = StandardTestDispatcher(scope.testScheduler)
    )

  private class GatedDashboardRepository(
    private val gate: CompletableDeferred<DashboardResponse>
  ) : DashboardRepository {
    override suspend fun getDashboard(): DashboardResponse = gate.await()
  }
}
