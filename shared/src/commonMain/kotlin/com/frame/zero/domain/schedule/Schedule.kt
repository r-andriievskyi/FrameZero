package com.frame.zero.domain.schedule

import com.frame.zero.dto.schedule.ScheduleDayDto
import com.frame.zero.dto.schedule.ScheduleEventDto
import com.frame.zero.dto.schedule.ScheduleResponse
import com.frame.zero.dto.schedule.ScheduleTaskDto
import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.dto.task.TaskStatus
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

enum class ScheduleView {
  DAY,
  WEEK,
  MONTH
}

data class Schedule(
  val rangeStart: LocalDate,
  val rangeEnd: LocalDate,
  val days: List<ScheduleDay>
)

data class ScheduleDay(
  val date: LocalDate,
  val events: List<ScheduleEvent>,
  val tasks: List<ScheduleTask>
)

data class ScheduleEvent(
  val id: String,
  val title: String,
  val productionId: String,
  val productionTitle: String,
  val startsAt: Instant,
  val endsAt: Instant,
  val location: String?,
  val kind: ScheduleEventKind
)

data class ScheduleTask(
  val id: String,
  val title: String,
  val productionId: String,
  val productionTitle: String,
  val dueDate: LocalDate,
  val status: TaskStatus,
  val priority: TaskPriority
)

fun ScheduleResponse.toDomain(): Schedule =
  Schedule(rangeStart = rangeStart, rangeEnd = rangeEnd, days = days.map { it.toDomain() })

fun ScheduleDayDto.toDomain(): ScheduleDay =
  ScheduleDay(
    date = date,
    events = events.map { it.toDomain() },
    tasks = tasks.map { it.toDomain() }
  )

fun ScheduleEventDto.toDomain(): ScheduleEvent =
  ScheduleEvent(
    id = id,
    title = title,
    productionId = productionId,
    productionTitle = productionTitle,
    startsAt = startsAt,
    endsAt = endsAt,
    location = location,
    kind = kind
  )

fun ScheduleTaskDto.toDomain(): ScheduleTask =
  ScheduleTask(
    id = id,
    title = title,
    productionId = productionId,
    productionTitle = productionTitle,
    dueDate = dueDate,
    status = status,
    priority = priority
  )
