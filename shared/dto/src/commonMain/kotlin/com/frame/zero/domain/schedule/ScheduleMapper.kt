package com.frame.zero.domain.schedule

import com.frame.zero.dto.schedule.ScheduleDayDto
import com.frame.zero.dto.schedule.ScheduleEventDto
import com.frame.zero.dto.schedule.ScheduleResponse
import com.frame.zero.dto.schedule.ScheduleTaskDto

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
