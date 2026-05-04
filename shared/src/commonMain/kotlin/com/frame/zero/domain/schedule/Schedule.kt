package com.frame.zero.domain.schedule

import com.frame.zero.dto.schedule.ScheduleDayDto
import com.frame.zero.dto.schedule.ScheduleItemDto
import com.frame.zero.dto.schedule.ScheduleItemSource
import com.frame.zero.dto.schedule.ScheduleResponse
import com.frame.zero.dto.task.TaskStatus
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

enum class ScheduleView {
  DAY,
  WEEK,
  MONTH,
}

data class Schedule(val rangeStart: LocalDate, val rangeEnd: LocalDate, val days: List<ScheduleDay>)

data class ScheduleDay(val date: LocalDate, val items: List<ScheduleItem>)

data class ScheduleItem(
  val id: String,
  val source: ScheduleItemSource,
  val title: String,
  val productionId: String,
  val productionTitle: String,
  val startsAt: Instant?,
  val endsAt: Instant?,
  val dueDate: LocalDate?,
  val location: String?,
  val eventKind: ScheduleEventKind?,
  val taskStatus: TaskStatus?,
)

fun ScheduleResponse.toDomain(): Schedule =
  Schedule(rangeStart = rangeStart, rangeEnd = rangeEnd, days = days.map { it.toDomain() })

fun ScheduleDayDto.toDomain(): ScheduleDay =
  ScheduleDay(date = date, items = items.map { it.toDomain() })

fun ScheduleItemDto.toDomain(): ScheduleItem =
  ScheduleItem(
    id = id,
    source = source,
    title = title,
    productionId = productionId,
    productionTitle = productionTitle,
    startsAt = startsAt,
    endsAt = endsAt,
    dueDate = dueDate,
    location = location,
    eventKind = eventKind,
    taskStatus = taskStatus,
  )
