package com.frame.zero.dto.schedule

import com.frame.zero.domain.schedule.ScheduleEventKind
import com.frame.zero.dto.task.TaskStatus
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ScheduleResponse(
  val rangeStart: LocalDate,
  val rangeEnd: LocalDate,
  val days: List<ScheduleDayDto>,
)

@Serializable data class ScheduleDayDto(val date: LocalDate, val items: List<ScheduleItemDto>)

@Serializable
enum class ScheduleItemSource {
  TASK,
  EVENT,
}

/**
 * Unified item shown on the schedule. Events carry [startsAt]/[endsAt]/[location]/[eventKind];
 * tasks carry [dueDate]/[taskStatus]. Fields not applicable to the source are null.
 */
@Serializable
data class ScheduleItemDto(
  val id: String,
  val source: ScheduleItemSource,
  val title: String,
  val productionId: String,
  val productionTitle: String,
  val startsAt: Instant? = null,
  val endsAt: Instant? = null,
  val dueDate: LocalDate? = null,
  val location: String? = null,
  val eventKind: ScheduleEventKind? = null,
  val taskStatus: TaskStatus? = null,
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
