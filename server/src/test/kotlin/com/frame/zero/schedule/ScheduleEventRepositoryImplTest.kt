package com.frame.zero.schedule

import com.frame.zero.auth.UserRepositoryImpl
import com.frame.zero.common.testing.PostgresTestDatabase
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.schedule.ScheduleEventKind
import com.frame.zero.production.ProductionMemberRepositoryImpl
import com.frame.zero.production.ProductionRepositoryImpl
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class ScheduleEventRepositoryImplTest {
  private val db = PostgresTestDatabase()
  private val users = UserRepositoryImpl()
  private val productions = ProductionRepositoryImpl()
  private val members = ProductionMemberRepositoryImpl()
  private val events = ScheduleEventRepositoryImpl()

  @BeforeTest
  fun setUp() {
    db.start()
  }

  @AfterTest
  fun tearDown() {
    db.stop()
  }

  private suspend fun newProduction(
    ownerId: UUID,
    title: String = "Film"
  ): UUID =
    productions.create(
      title = title,
      genre = Genre.DRAMA,
      logline = null,
      phase = ProductionPhase.PRODUCTION,
      startDate = LocalDate(2026, 1, 1),
      wrapDate = LocalDate(2026, 12, 31),
      budgetCents = null,
      ownerUserId = ownerId
    ).id

  /** The user must be a production *member* to see its events — being owner is not enough. */
  private suspend fun memberWithProduction(email: String = "user@x.com"): Pair<UUID, UUID> {
    val userId = users.create(email, "h", "Us", "Er").id
    val productionId = newProduction(userId)
    members.add(productionId, userId, "Us Er", "Director", email)
    return userId to productionId
  }

  @Test
  fun `create persists the event and resolves the production title`() =
    runBlocking {
      val (_, productionId) = memberWithProduction()

      val event =
        events.create(
          productionId = productionId,
          title = "Table read",
          location = "Stage 1",
          startsAt = Instant.parse("2026-05-04T10:00:00Z"),
          endsAt = Instant.parse("2026-05-04T12:00:00Z"),
          kind = ScheduleEventKind.MEETING
        )

      assertEquals("Table read", event.title)
      assertEquals("Film", event.productionTitle, "title must be joined from the production row")
      assertEquals(ScheduleEventKind.MEETING, event.kind)
      assertEquals(event, events.findById(event.id))
    }

  @Test
  fun `findInRangeForUser returns member events within the half-open range ordered by start`() =
    runBlocking {
      val (userId, productionId) = memberWithProduction()
      val rangeStart = Instant.parse("2026-05-04T00:00:00Z")
      val rangeEnd = Instant.parse("2026-05-05T00:00:00Z")

      val morning = events.create(
        productionId,
        "Morning",
        null,
        Instant.parse("2026-05-04T09:00:00Z"),
        Instant.parse("2026-05-04T10:00:00Z"),
        ScheduleEventKind.SHOOT
      )
      val afternoon = events.create(
        productionId,
        "Afternoon",
        null,
        Instant.parse("2026-05-04T15:00:00Z"),
        Instant.parse("2026-05-04T16:00:00Z"),
        ScheduleEventKind.SHOOT
      )

      val result = events.findInRangeForUser(userId, rangeStart, rangeEnd)

      assertEquals(
        listOf(morning.id, afternoon.id),
        result.map { it.id },
        "events inside the day, ordered by start time"
      )
    }

  @Test
  fun `findInRangeForUser includes the start boundary and excludes the end boundary`() =
    runBlocking {
      val (userId, productionId) = memberWithProduction()
      val rangeStart = Instant.parse("2026-05-04T00:00:00Z")
      val rangeEnd = Instant.parse("2026-05-05T00:00:00Z")

      // Exactly at rangeStart -> included (>=). Exactly at rangeEnd -> excluded (<).
      val atStart = events.create(
        productionId,
        "AtStart",
        null,
        rangeStart,
        rangeStart.plusOneHour(),
        ScheduleEventKind.OTHER
      )
      events.create(
        productionId,
        "AtEnd",
        null,
        rangeEnd,
        rangeEnd.plusOneHour(),
        ScheduleEventKind.OTHER
      )
      events.create(
        productionId,
        "Before",
        null,
        Instant.parse("2026-05-03T23:59:59Z"),
        rangeStart,
        ScheduleEventKind.OTHER
      )

      val result = events.findInRangeForUser(userId, rangeStart, rangeEnd)

      assertEquals(listOf(atStart.id), result.map { it.id }, "only the event starting at rangeStart")
    }

  @Test
  fun `findInRangeForUser excludes events from productions the user is not a member of`() =
    runBlocking {
      val (userId, _) = memberWithProduction("user@x.com")
      val otherOwner = users.create("other@x.com", "h", "Ot", "Her").id
      val otherProduction = newProduction(otherOwner, title = "Secret")
      events.create(
        otherProduction,
        "Hidden",
        null,
        Instant.parse("2026-05-04T10:00:00Z"),
        Instant.parse("2026-05-04T12:00:00Z"),
        ScheduleEventKind.SHOOT
      )

      val result =
        events.findInRangeForUser(
          userId,
          Instant.parse("2026-05-04T00:00:00Z"),
          Instant.parse("2026-05-05T00:00:00Z")
        )

      assertTrue(result.isEmpty(), "events of non-member productions must not leak")
    }

  @Test
  fun `findInRangeForUser excludes events of soft-deleted productions`() =
    runBlocking {
      val (userId, productionId) = memberWithProduction()
      events.create(
        productionId,
        "Doomed",
        null,
        Instant.parse("2026-05-04T10:00:00Z"),
        Instant.parse("2026-05-04T12:00:00Z"),
        ScheduleEventKind.SHOOT
      )
      productions.softDelete(productionId)

      val result =
        events.findInRangeForUser(
          userId,
          Instant.parse("2026-05-04T00:00:00Z"),
          Instant.parse("2026-05-05T00:00:00Z")
        )

      assertTrue(result.isEmpty(), "soft-deleted production's events must be hidden")
    }

  @Test
  fun `findInRangeForUser is empty when the user is a member of nothing`() =
    runBlocking {
      val loner = users.create("loner@x.com", "h", "Lo", "Ner").id

      val result =
        events.findInRangeForUser(
          loner,
          Instant.parse("2026-05-04T00:00:00Z"),
          Instant.parse("2026-05-05T00:00:00Z")
        )

      assertTrue(result.isEmpty())
    }

  @Test
  fun `update mutates only the provided fields`() =
    runBlocking {
      val (_, productionId) = memberWithProduction()
      val event = events.create(
        productionId,
        "Before",
        "Stage 1",
        Instant.parse("2026-05-04T10:00:00Z"),
        Instant.parse("2026-05-04T12:00:00Z"),
        ScheduleEventKind.MEETING
      )

      val updated = assertNotNull(events.update(event.id, title = "After", null, null, null, null))

      assertEquals("After", updated.title)
      assertEquals("Stage 1", updated.location, "an omitted field must be left untouched")
      assertEquals(ScheduleEventKind.MEETING, updated.kind)
    }

  @Test
  fun `delete removes the event`() =
    runBlocking {
      val (_, productionId) = memberWithProduction()
      val event = events.create(
        productionId,
        "Doomed",
        null,
        Instant.parse("2026-05-04T10:00:00Z"),
        Instant.parse("2026-05-04T12:00:00Z"),
        ScheduleEventKind.SHOOT
      )

      assertTrue(events.delete(event.id))
      assertNull(events.findById(event.id))
    }

  private fun Instant.plusOneHour(): Instant = Instant.fromEpochSeconds(epochSeconds + 3600)
}
