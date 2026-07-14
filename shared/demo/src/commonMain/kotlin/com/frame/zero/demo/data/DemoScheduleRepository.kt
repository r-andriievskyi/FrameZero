package com.frame.zero.demo.data

import com.frame.zero.demo.DemoDataStore
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.domain.schedule.ScheduleDay
import com.frame.zero.domain.schedule.ScheduleEvent
import com.frame.zero.domain.schedule.ScheduleEventKind
import com.frame.zero.domain.schedule.ScheduleTask
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.repository.schedule.ScheduleRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

internal class DemoScheduleRepository(
  private val store: DemoDataStore
) : ScheduleRepository {
  private val tz = TimeZone.currentSystemDefault()

  override suspend fun getSchedule(
    view: ScheduleView,
    date: LocalDate
  ): Schedule {
    val (start, end) = range(view, date)
    val tasks = store.tasks.value
    val productions = store.productions.value

    val days = (0..start.daysUntil(end)).map { offset ->
      val day = start.plus(offset, DateTimeUnit.DAY)
      ScheduleDay(
        date = day,
        events = eventsOn(day, productions),
        tasks = tasks
          .filter { it.dueDate == day }
          .map {
            ScheduleTask(
              id = it.id,
              title = it.title,
              productionId = it.productionId,
              productionTitle = it.productionTitle,
              dueDate = day,
              status = it.status,
              priority = it.priority
            )
          }
      )
    }
    return Schedule(rangeStart = start, rangeEnd = end, days = days)
  }

  private fun range(
    view: ScheduleView,
    date: LocalDate
  ): Pair<LocalDate, LocalDate> =
    when (view) {
      ScheduleView.DAY -> date to date
      ScheduleView.WEEK -> {
        val start = date.minus(date.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
        start to start.plus(6, DateTimeUnit.DAY)
      }
      ScheduleView.MONTH -> {
        val start = LocalDate(date.year, date.month, 1)
        start to start.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
      }
    }

  private fun eventsOn(
    day: LocalDate,
    productions: List<ProductionDetail>
  ): List<ScheduleEvent> {
    val active = productions.filter {
      it.phase != ProductionPhase.ARCHIVED && day >= it.startDate && day <= it.wrapDate
    }
    return active.mapIndexedNotNull { index, production ->
      if ((day.toEpochDays() + index) % 3 != 0L) return@mapIndexedNotNull null
      val kind = when (production.phase) {
        ProductionPhase.PRODUCTION -> ScheduleEventKind.SHOOT
        ProductionPhase.POST_PRODUCTION -> ScheduleEventKind.REVIEW
        else -> ScheduleEventKind.MEETING
      }
      val startHour = 9 + index
      ScheduleEvent(
        id = "evt-${production.id}-$day",
        title = eventTitle(kind, production.title),
        productionId = production.id,
        productionTitle = production.title,
        startsAt = LocalDateTime(day, LocalTime(startHour, 0)).toInstant(tz),
        endsAt = LocalDateTime(day, LocalTime(startHour + 2, 0)).toInstant(tz),
        location = eventLocation(kind),
        kind = kind
      )
    }
  }

  private fun eventTitle(
    kind: ScheduleEventKind,
    production: String
  ): String =
    when (kind) {
      ScheduleEventKind.SHOOT -> "$production — shoot day"
      ScheduleEventKind.REVIEW -> "$production — editorial review"
      ScheduleEventKind.MEETING -> "$production — production meeting"
      ScheduleEventKind.OTHER -> production
    }

  private fun eventLocation(kind: ScheduleEventKind): String? =
    when (kind) {
      ScheduleEventKind.SHOOT -> "Stage 4"
      ScheduleEventKind.REVIEW -> "Edit Bay 2"
      ScheduleEventKind.MEETING -> "Production Office"
      ScheduleEventKind.OTHER -> null
    }
}
