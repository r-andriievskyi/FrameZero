package com.frame.zero.feature.home.usecase

import com.frame.zero.domain.OfflineException
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.domain.schedule.ScheduleDay
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.testing.FakeScheduleRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetScheduleUseCaseTest {
  private val date = LocalDate(2026, 3, 5)

  @Test
  fun `forwards the view and date to the repository`() =
    runTest {
      val repo = FakeScheduleRepository()

      GetScheduleUseCase(repo)(GetScheduleUseCase.Params(ScheduleView.MONTH, date))

      assertEquals(FakeScheduleRepository.Call(view = ScheduleView.MONTH, date = date), repo.calls.single())
    }

  @Test
  fun `success returns the domain schedule`() =
    runTest {
      val repo = FakeScheduleRepository(
        schedule = Schedule(
          rangeStart = LocalDate(2026, 3, 1),
          rangeEnd = LocalDate(2026, 3, 31),
          days = listOf(ScheduleDay(date = date, events = emptyList(), tasks = emptyList()))
        )
      )

      val outcome = GetScheduleUseCase(repo)(GetScheduleUseCase.Params(ScheduleView.MONTH, date))

      val success = assertIs<Outcome.Success<Schedule>>(outcome)
      assertEquals(LocalDate(2026, 3, 1), success.data.rangeStart)
      assertEquals(LocalDate(2026, 3, 31), success.data.rangeEnd)
      assertEquals(listOf(date), success.data.days.map { it.date })
    }

  @Test
  fun `failure is mapped to a domain error`() =
    runTest {
      val repo = FakeScheduleRepository(throws = OfflineException("offline"))

      val outcome = GetScheduleUseCase(repo)(GetScheduleUseCase.Params(ScheduleView.DAY, date))

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Offline("offline"), failure.error)
    }
}
