package com.frame.zero.domain.schedule

import com.frame.zero.domain.task.TaskPriority
import com.frame.zero.domain.task.TaskStatus
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
