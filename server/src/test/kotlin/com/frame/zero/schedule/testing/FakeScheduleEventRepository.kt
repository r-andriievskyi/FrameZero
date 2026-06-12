package com.frame.zero.schedule.testing

import com.frame.zero.domain.schedule.ScheduleEventKind
import com.frame.zero.schedule.ScheduleEventRecord
import com.frame.zero.schedule.ScheduleEventRepository
import kotlin.time.Instant
import java.util.UUID

internal class FakeScheduleEventRepository : ScheduleEventRepository {
  val events: MutableList<ScheduleEventRecord> = mutableListOf()

  override suspend fun findInRangeForUser(
    userId: UUID,
    rangeStart: Instant,
    rangeEnd: Instant
  ): List<ScheduleEventRecord> =
    events.filter { e ->
      e.startsAt >= rangeStart && e.startsAt < rangeEnd
    }

  override suspend fun findById(id: UUID): ScheduleEventRecord? = events.firstOrNull { it.id == id }

  override suspend fun create(
    productionId: UUID,
    title: String,
    location: String?,
    startsAt: Instant,
    endsAt: Instant,
    kind: ScheduleEventKind
  ): ScheduleEventRecord {
    val record =
      ScheduleEventRecord(
        id = UUID.randomUUID(),
        productionId = productionId,
        productionTitle = "Test Production",
        title = title,
        location = location,
        startsAt = startsAt,
        endsAt = endsAt,
        kind = kind
      )
    events += record
    return record
  }

  override suspend fun update(
    id: UUID,
    title: String?,
    location: String?,
    startsAt: Instant?,
    endsAt: Instant?,
    kind: ScheduleEventKind?
  ): ScheduleEventRecord? {
    val idx = events.indexOfFirst { it.id == id }
    if (idx < 0) return null
    val updated =
      events[idx].copy(
        title = title ?: events[idx].title,
        location = location ?: events[idx].location,
        startsAt = startsAt ?: events[idx].startsAt,
        endsAt = endsAt ?: events[idx].endsAt,
        kind = kind ?: events[idx].kind
      )
    events[idx] = updated
    return updated
  }

  override suspend fun delete(id: UUID): Boolean {
    val idx = events.indexOfFirst { it.id == id }
    if (idx < 0) return false
    events.removeAt(idx)
    return true
  }
}
