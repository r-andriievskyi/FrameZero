package com.frame.zero.schedule

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.common.testing.NoopTransactor
import com.frame.zero.domain.schedule.ScheduleEventKind
import com.frame.zero.production.ProductionAccessService
import com.frame.zero.production.testing.FakeProductionMemberRepository
import com.frame.zero.production.testing.FakeProductionRepository
import com.frame.zero.schedule.testing.FakeScheduleEventRepository
import com.frame.zero.task.testing.FakeTaskRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

class ScheduleServiceTest {
  private val events = FakeScheduleEventRepository()
  private val tasks = FakeTaskRepository()
  private val service = ScheduleService(
    events = events,
    tasks = tasks,
    access = ProductionAccessService(FakeProductionRepository(), FakeProductionMemberRepository()),
    transactor = NoopTransactor()
  )

  private val userId = UUID.randomUUID()
  private val productionId = UUID.randomUUID()

  private suspend fun seedEvent(
    startsAt: Instant,
    title: String = "Event"
  ) {
    events.create(
      productionId = productionId,
      title = title,
      location = null,
      startsAt = startsAt,
      endsAt = Instant.fromEpochSeconds(startsAt.epochSeconds + 3600),
      kind = ScheduleEventKind.SHOOT
    )
  }

  private suspend fun seedTask(
    dueDate: LocalDate,
    title: String = "Task"
  ) {
    tasks.create(productionId, title, null, dueDate, userId)
  }

  @Test
  fun `day view returns a single day holding the events and tasks that fall on it`() =
    runTest {
      seedEvent(Instant.parse("2026-05-05T10:00:00Z"), "Shoot")
      seedTask(LocalDate(2026, 5, 5), "Lock script")

      val response = service.get(userId, view = "day", dateParam = "2026-05-05", timezone = TimeZone.UTC)

      assertEquals(LocalDate(2026, 5, 5), response.rangeStart)
      assertEquals(LocalDate(2026, 5, 5), response.rangeEnd)
      assertEquals(1, response.days.size)
      val day = response.days.single()
      assertEquals(LocalDate(2026, 5, 5), day.date)
      assertEquals(listOf("Shoot"), day.events.map { it.title })
      assertEquals(listOf("Lock script"), day.tasks.map { it.title })
    }

  @Test
  fun `week view spans Monday through Sunday and buckets each item into its own day`() =
    runTest {
      seedEvent(Instant.parse("2026-05-05T10:00:00Z"))
      seedTask(LocalDate(2026, 5, 5))

      val response = service.get(userId, view = "week", dateParam = "2026-05-05", timezone = TimeZone.UTC)

      assertEquals(7, response.days.size)
      assertEquals(DayOfWeek.MONDAY, response.rangeStart.dayOfWeek)
      assertEquals(DayOfWeek.SUNDAY, response.rangeEnd.dayOfWeek)
      assertTrue(response.rangeStart <= LocalDate(2026, 5, 5) && LocalDate(2026, 5, 5) <= response.rangeEnd)

      val target = response.days.single { it.date == LocalDate(2026, 5, 5) }
      assertEquals(1, target.events.size)
      assertEquals(1, target.tasks.size)
      // Exactly one bucket holds the event and one holds the task — no duplication across days.
      assertEquals(1, response.days.sumOf { it.events.size })
      assertEquals(1, response.days.sumOf { it.tasks.size })
      assertTrue(
        response.days.filter { it.date != LocalDate(2026, 5, 5) }.all { it.events.isEmpty() && it.tasks.isEmpty() }
      )
    }

  @Test
  fun `an event at midnight buckets into that day, not the previous one`() =
    runTest {
      seedEvent(Instant.parse("2026-05-05T00:00:00Z"))

      val response = service.get(userId, view = "week", dateParam = "2026-05-05", timezone = TimeZone.UTC)

      val dayWithEvent = response.days.single { it.events.isNotEmpty() }
      assertEquals(LocalDate(2026, 5, 5), dayWithEvent.date, "a midnight start belongs to its own day")
    }

  @Test
  fun `month view spans the whole calendar month`() =
    runTest {
      val response = service.get(userId, view = "month", dateParam = "2026-05", timezone = TimeZone.UTC)

      assertEquals(LocalDate(2026, 5, 1), response.rangeStart)
      assertEquals(LocalDate(2026, 5, 31), response.rangeEnd)
      assertEquals(31, response.days.size)
    }

  @Test
  fun `an unknown view is rejected as a validation error`() =
    runTest {
      val ex =
        assertFailsWith<AppException> {
          service.get(userId, view = "decade", dateParam = "2026-05-05", timezone = TimeZone.UTC)
        }
      val error = assertIs<AppError.ValidationError>(ex.error)
      assertTrue(error.fields.containsKey("view"))
    }

  @Test
  fun `a malformed date is rejected as a validation error`() =
    runTest {
      val ex =
        assertFailsWith<AppException> {
          service.get(userId, view = "day", dateParam = "not-a-date", timezone = TimeZone.UTC)
        }
      val error = assertIs<AppError.ValidationError>(ex.error)
      assertTrue(error.fields.containsKey("date"))
    }
}
