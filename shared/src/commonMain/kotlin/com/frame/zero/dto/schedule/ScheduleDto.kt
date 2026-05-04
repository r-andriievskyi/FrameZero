package com.frame.zero.dto.schedule

import com.frame.zero.domain.schedule.ScheduleEventKind
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ScheduleResponse(
  val rangeStart: LocalDate,
  val rangeEnd: LocalDate,
  val days: List<ScheduleDayDto>,
)

@Serializable
data class ScheduleDayDto(
  val date: LocalDate,
  val events: List<ScheduleEventDto>,
)

@Serializable
data class ScheduleEventDto(
  val id: String,
  val title: String,
  val location: String?,
  val startsAt: Instant,
  val endsAt: Instant,
  val kind: ScheduleEventKind,
  val productionId: String,
  val productionTitle: String,
)

@Serializable
data class CreateScheduleEventRequest(
  val productionId: String,
  val title: String,
  val location: String? = null,
  val startsAt: Instant,
  val endsAt: Instant,
  val kind: ScheduleEventKind,
)

@Serializable
data class UpdateScheduleEventRequest(
  val title: String? = null,
  val location: String? = null,
  val startsAt: Instant? = null,
  val endsAt: Instant? = null,
  val kind: ScheduleEventKind? = null,
)
