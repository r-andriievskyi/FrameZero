package com.frame.zero.feature.home.tab.schedule

import app.cash.turbine.test
import com.frame.zero.domain.OfflineException
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.testing.FakeConnectivityObserver
import com.frame.zero.testing.FakeScheduleRepository
import com.frame.zero.feature.home.usecase.GetScheduleUseCase
import com.frame.zero.repository.schedule.ScheduleRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
class ScheduleTabViewModelTest {
  private val testDispatcher = StandardTestDispatcher()

  private val scheduleResponse = Schedule(
    rangeStart = LocalDate(2026, 5, 1),
    rangeEnd = LocalDate(2026, 5, 7),
    days = emptyList()
  )

  @Test
  fun `init loads schedule for today using DAY view and clears loading`() =
    runTest(testDispatcher) {
      val repo = FakeScheduleRepository(schedule = scheduleResponse)
      val viewModel = makeViewModel(repo)

      viewModel.state.test {
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.schedule)
        assertEquals(ScheduleView.DAY, state.view)
        val today = assertNotNull(state.selectedDate)
        val call = repo.calls.single()
        assertEquals(ScheduleView.DAY, call.view)
        assertEquals(today, call.date)
      }
    }

  @Test
  fun `init sets isLoading true before completion`() =
    runTest(testDispatcher) {
      val gate = CompletableDeferred<Schedule>()
      val repo = GatedScheduleRepository(gate)
      val viewModel = makeViewModel(repo)

      viewModel.state.test {
        runCurrent()

        assertTrue(expectMostRecentItem().isLoading)

        gate.complete(scheduleResponse)
        advanceUntilIdle()

        assertFalse(expectMostRecentItem().isLoading)
      }
    }

  @Test
  fun `offline failure sets a Network error and leaves schedule null`() =
    runTest(testDispatcher) {
      val repo = FakeScheduleRepository(throws = OfflineException())
      val viewModel = makeViewModel(repo)

      viewModel.state.test {
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertNull(state.schedule)
        assertNotNull(state.error)
        assertTrue(state.error.autoRetries)
        assertFalse(state.isLoading)
      }
    }

  @Test
  fun `connection failure while online sets a Generic error`() =
    runTest(testDispatcher) {
      val repo = FakeScheduleRepository(throws = IOException("connection refused"))
      val viewModel = makeViewModel(repo)

      viewModel.state.test {
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertNull(state.schedule)
        assertNotNull(state.error)
        assertFalse(state.error.autoRetries)
        assertFalse(state.isLoading)
      }
    }

  @Test
  fun `reconnecting after a network failure auto-reloads the schedule`() =
    runTest(testDispatcher) {
      var shouldFail = true
      val repo = object : ScheduleRepository {
        var calls = 0

        override suspend fun getSchedule(
          view: ScheduleView,
          date: LocalDate
        ): Schedule {
          calls++
          if (shouldFail) throw OfflineException()
          return scheduleResponse
        }
      }
      val connectivity = FakeConnectivityObserver(initiallyOnline = false)
      val viewModel = ScheduleTabViewModel(
        getScheduleUseCase = GetScheduleUseCase(repo),
        connectivityObserver = connectivity,
        dispatcher = testDispatcher
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
        assertNotNull(recovered.schedule)
      }
      assertEquals(2, repo.calls)
    }

  @Test
  fun `onViewChanged with the same view does not trigger a reload`() =
    runTest(testDispatcher) {
      val repo = FakeScheduleRepository(schedule = scheduleResponse)
      val viewModel = makeViewModel(repo)
      advanceUntilIdle()
      val callsBefore = repo.calls.size

      viewModel.state.test {
        viewModel.onIntent(ScheduleTabIntent.ViewChanged(ScheduleView.DAY))
        advanceUntilIdle()

        assertEquals(ScheduleView.DAY, expectMostRecentItem().view)
      }
      assertEquals(callsBefore, repo.calls.size)
    }

  @Test
  fun `onViewChanged with a new view updates state and reloads using the new view`() =
    runTest(testDispatcher) {
      val repo = FakeScheduleRepository(schedule = scheduleResponse)
      val viewModel = makeViewModel(repo)
      advanceUntilIdle()

      viewModel.state.test {
        viewModel.onIntent(ScheduleTabIntent.ViewChanged(ScheduleView.WEEK))
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertEquals(ScheduleView.WEEK, state.view)
        assertEquals(2, repo.calls.size)
        assertEquals(ScheduleView.WEEK, repo.calls.last().view)
        assertEquals(state.selectedDate, repo.calls.last().date)
      }
    }

  @Test
  fun `onViewChanged to MONTH reloads with the selected date`() =
    runTest(testDispatcher) {
      val repo = FakeScheduleRepository(schedule = scheduleResponse)
      val viewModel = makeViewModel(repo)
      viewModel.state.test {
        viewModel.onIntent(ScheduleTabIntent.ViewChanged(ScheduleView.MONTH))
        advanceUntilIdle()

        val date = assertNotNull(expectMostRecentItem().selectedDate)
        assertEquals(ScheduleView.MONTH, repo.calls.last().view)
        assertEquals(date, repo.calls.last().date)
      }
    }

  @Test
  fun `onDateSelected updates selectedDate and reloads with the new date`() =
    runTest(testDispatcher) {
      val repo = FakeScheduleRepository(schedule = scheduleResponse)
      val viewModel = makeViewModel(repo)
      advanceUntilIdle()
      val newDate = LocalDate(2026, 4, 1)

      viewModel.state.test {
        viewModel.onIntent(ScheduleTabIntent.DateSelected(newDate))
        advanceUntilIdle()

        assertEquals(newDate, expectMostRecentItem().selectedDate)
        assertEquals(2, repo.calls.size)
        assertEquals(ScheduleView.DAY, repo.calls.last().view)
        assertEquals(newDate, repo.calls.last().date)
      }
    }

  @Test
  fun `onDestroy cancels scope so subsequent date changes do not call the repository`() =
    runTest(testDispatcher) {
      val repo = FakeScheduleRepository(schedule = scheduleResponse)
      val viewModel = makeViewModel(repo)
      advanceUntilIdle()
      val callsBeforeDestroy = repo.calls.size

      viewModel.onDestroy()
      viewModel.onIntent(ScheduleTabIntent.DateSelected(LocalDate(2026, 4, 1)))
      advanceUntilIdle()

      assertEquals(callsBeforeDestroy, repo.calls.size)
    }

  private fun makeViewModel(repository: ScheduleRepository): ScheduleTabViewModel =
    ScheduleTabViewModel(
      getScheduleUseCase = GetScheduleUseCase(repository),
      connectivityObserver = FakeConnectivityObserver(),
      dispatcher = testDispatcher
    )

  private class GatedScheduleRepository(
    private val gate: CompletableDeferred<Schedule>
  ) : ScheduleRepository {
    override suspend fun getSchedule(
      view: ScheduleView,
      date: LocalDate
    ): Schedule = gate.await()
  }
}
