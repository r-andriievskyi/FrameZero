package com.frame.zero.feature.home.tab.schedule

import com.frame.zero.core.network.connectivity.OfflineException
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.dto.schedule.ScheduleResponse
import com.frame.zero.feature.home.LoadErrorKind
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
import kotlinx.datetime.number
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

  private val scheduleResponse = ScheduleResponse(
    rangeStart = LocalDate(2026, 5, 1),
    rangeEnd = LocalDate(2026, 5, 7),
    days = emptyList()
  )

  @Test
  fun `init loads schedule for today using DAY view and clears loading`() =
    runTest(testDispatcher) {
      val repo = FakeScheduleRepository(response = scheduleResponse)
      val viewModel = makeViewModel(repo)

      advanceUntilIdle()

      assertFalse(viewModel.state.value.isLoading)
      assertNull(viewModel.state.value.error)
      assertNotNull(viewModel.state.value.schedule)
      assertEquals(ScheduleView.DAY, viewModel.state.value.view)
      val today = assertNotNull(viewModel.state.value.selectedDate)
      val call = repo.calls.single()
      assertEquals("day", call.view)
      assertEquals(today.toString(), call.date)
    }

  @Test
  fun `init sets isLoading true before completion`() =
    runTest(testDispatcher) {
      val gate = CompletableDeferred<ScheduleResponse>()
      val repo = GatedScheduleRepository(gate)
      val viewModel = makeViewModel(repo)

      runCurrent()

      assertTrue(viewModel.state.value.isLoading)

      gate.complete(scheduleResponse)
      advanceUntilIdle()

      assertFalse(viewModel.state.value.isLoading)
    }

  @Test
  fun `offline failure sets a Network error and leaves schedule null`() =
    runTest(testDispatcher) {
      val repo = FakeScheduleRepository(throws = OfflineException())
      val viewModel = makeViewModel(repo)

      advanceUntilIdle()

      assertNull(viewModel.state.value.schedule)
      assertEquals(LoadErrorKind.Network, viewModel.state.value.error)
      assertFalse(viewModel.state.value.isLoading)
    }

  @Test
  fun `connection failure while online sets a Generic error`() =
    runTest(testDispatcher) {
      val repo = FakeScheduleRepository(throws = IOException("connection refused"))
      val viewModel = makeViewModel(repo)

      advanceUntilIdle()

      assertNull(viewModel.state.value.schedule)
      assertEquals(LoadErrorKind.Generic, viewModel.state.value.error)
      assertFalse(viewModel.state.value.isLoading)
    }

  @Test
  fun `reconnecting after a network failure auto-reloads the schedule`() =
    runTest(testDispatcher) {
      var shouldFail = true
      val repo = object : ScheduleRepository {
        var calls = 0

        override suspend fun getSchedule(
          view: String,
          date: String
        ): ScheduleResponse {
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

      advanceUntilIdle()
      assertEquals(LoadErrorKind.Network, viewModel.state.value.error)

      shouldFail = false
      connectivity.online.value = true
      advanceUntilIdle()

      assertNull(viewModel.state.value.error)
      assertNotNull(viewModel.state.value.schedule)
      assertEquals(2, repo.calls)
    }

  @Test
  fun `onViewChanged with the same view does not trigger a reload`() =
    runTest(testDispatcher) {
      val repo = FakeScheduleRepository(response = scheduleResponse)
      val viewModel = makeViewModel(repo)
      advanceUntilIdle()
      val callsBefore = repo.calls.size

      viewModel.onViewChanged(ScheduleView.DAY)
      advanceUntilIdle()

      assertEquals(callsBefore, repo.calls.size)
      assertEquals(ScheduleView.DAY, viewModel.state.value.view)
    }

  @Test
  fun `onViewChanged with a new view updates state and reloads using the new view`() =
    runTest(testDispatcher) {
      val repo = FakeScheduleRepository(response = scheduleResponse)
      val viewModel = makeViewModel(repo)
      advanceUntilIdle()

      viewModel.onViewChanged(ScheduleView.WEEK)
      advanceUntilIdle()

      assertEquals(ScheduleView.WEEK, viewModel.state.value.view)
      assertEquals(2, repo.calls.size)
      assertEquals("week", repo.calls.last().view)
      assertEquals(viewModel.state.value.selectedDate.toString(), repo.calls.last().date)
    }

  @Test
  fun `onViewChanged to MONTH formats the date param as yyyy-MM`() =
    runTest(testDispatcher) {
      val repo = FakeScheduleRepository(response = scheduleResponse)
      val viewModel = makeViewModel(repo)
      advanceUntilIdle()

      viewModel.onViewChanged(ScheduleView.MONTH)
      advanceUntilIdle()

      val date = assertNotNull(viewModel.state.value.selectedDate)
      val expected = "${date.year}-${date.month.number.toString().padStart(2, '0')}"
      assertEquals("month", repo.calls.last().view)
      assertEquals(expected, repo.calls.last().date)
    }

  @Test
  fun `onDateSelected updates selectedDate and reloads with the new date`() =
    runTest(testDispatcher) {
      val repo = FakeScheduleRepository(response = scheduleResponse)
      val viewModel = makeViewModel(repo)
      advanceUntilIdle()
      val newDate = LocalDate(2026, 4, 1)

      viewModel.onDateSelected(newDate)
      advanceUntilIdle()

      assertEquals(newDate, viewModel.state.value.selectedDate)
      assertEquals(2, repo.calls.size)
      assertEquals("day", repo.calls.last().view)
      assertEquals(newDate.toString(), repo.calls.last().date)
    }

  @Test
  fun `onDestroy cancels scope so subsequent date changes do not call the repository`() =
    runTest(testDispatcher) {
      val repo = FakeScheduleRepository(response = scheduleResponse)
      val viewModel = makeViewModel(repo)
      advanceUntilIdle()
      val callsBeforeDestroy = repo.calls.size

      viewModel.onDestroy()
      viewModel.onDateSelected(LocalDate(2026, 4, 1))
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
    private val gate: CompletableDeferred<ScheduleResponse>
  ) : ScheduleRepository {
    override suspend fun getSchedule(
      view: String,
      date: String
    ): ScheduleResponse = gate.await()
  }
}
