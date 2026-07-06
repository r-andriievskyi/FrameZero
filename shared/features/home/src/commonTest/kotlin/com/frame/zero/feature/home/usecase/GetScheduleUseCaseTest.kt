package com.frame.zero.feature.home.usecase

import com.frame.zero.domain.OfflineException
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.dto.schedule.ScheduleDayDto
import com.frame.zero.dto.schedule.ScheduleResponse
import com.frame.zero.testing.FakeScheduleRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetScheduleUseCaseTest {
  private val date = LocalDate(2026, 3, 5)

  @Test
  fun `month view sends a lowercased view and a year-month date param`() =
    runTest {
      val repo = FakeScheduleRepository()

      GetScheduleUseCase(repo)(GetScheduleUseCase.Params(ScheduleView.MONTH, date))

      assertEquals(FakeScheduleRepository.Call(view = "month", date = "2026-03"), repo.calls.single())
    }

  @Test
  fun `day view sends the full iso date`() =
    runTest {
      val repo = FakeScheduleRepository()

      GetScheduleUseCase(repo)(GetScheduleUseCase.Params(ScheduleView.DAY, date))

      assertEquals(FakeScheduleRepository.Call(view = "day", date = "2026-03-05"), repo.calls.single())
    }

  @Test
  fun `week view sends the full iso date`() =
    runTest {
      val repo = FakeScheduleRepository()

      GetScheduleUseCase(repo)(GetScheduleUseCase.Params(ScheduleView.WEEK, date))

      assertEquals(FakeScheduleRepository.Call(view = "week", date = "2026-03-05"), repo.calls.single())
    }

  @Test
  fun `success maps the response to the domain schedule`() =
    runTest {
      val repo = FakeScheduleRepository(
        response = ScheduleResponse(
          rangeStart = LocalDate(2026, 3, 1),
          rangeEnd = LocalDate(2026, 3, 31),
          days = listOf(ScheduleDayDto(date = date, events = emptyList(), tasks = emptyList()))
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
