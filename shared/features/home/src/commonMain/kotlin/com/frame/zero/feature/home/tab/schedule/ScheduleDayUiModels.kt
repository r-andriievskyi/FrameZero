package com.frame.zero.feature.home.tab.schedule

import com.frame.zero.domain.schedule.ScheduleEventKind
import com.frame.zero.domain.task.TaskPriority
import kotlinx.datetime.LocalDate

sealed interface DueLabel {
  data object Today : DueLabel

  data class OtherDate(
    val date: LocalDate
  ) : DueLabel
}

data class ScheduleEventUiModel(
  val id: String,
  val title: String,
  val productionTitle: String,
  val location: String?,
  val eventKind: ScheduleEventKind,
  val timeRangeLabel: String
)

data class ScheduleTaskUiModel(
  val id: String,
  val title: String,
  val productionTitle: String,
  val priority: TaskPriority,
  val dueLabel: DueLabel
)
