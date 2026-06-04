package com.frame.zero.feature.task.details

import com.frame.zero.dto.task.TaskDetailDto
import kotlinx.datetime.LocalDate
import com.frame.zero.dto.task.TaskPriority as DtoTaskPriority
import com.frame.zero.dto.task.TaskStatus as DtoTaskStatus

private val MonthAbbreviations = arrayOf(
  "Jan",
  "Feb",
  "Mar",
  "Apr",
  "May",
  "Jun",
  "Jul",
  "Aug",
  "Sep",
  "Oct",
  "Nov",
  "Dec"
)

fun TaskDetailDto.toTaskDetailsState(today: LocalDate): TaskDetailsState {
  val mappedStatus = status.toFeatureStatus()
  return TaskDetailsState(
    taskId = id,
    title = title,
    productionName = productionTitle,
    priority = priority.toFeaturePriority(),
    status = mappedStatus,
    assignee = assignee?.let { member ->
      TaskMember(
        initials = initialsFrom(member.name),
        name = member.name,
        avatarColorHex = member.avatarColorHex
      )
    },
    dueDate = dueDate?.let { formatDueDate(it) },
    isDueToday = dueDate == today,
    description = description.orEmpty(),
    isLoading = false,
    isError = false,
    showMarkCompleteButton = mappedStatus != TaskStatus.COMPLETED
  )
}

private fun DtoTaskStatus.toFeatureStatus(): TaskStatus =
  when (this) {
    DtoTaskStatus.OPEN -> TaskStatus.IN_PROGRESS
    DtoTaskStatus.DONE -> TaskStatus.COMPLETED
  }

private fun DtoTaskPriority.toFeaturePriority(): TaskPriority =
  when (this) {
    DtoTaskPriority.HIGH -> TaskPriority.HIGH
    DtoTaskPriority.MEDIUM -> TaskPriority.MEDIUM
    DtoTaskPriority.LOW -> TaskPriority.LOW
  }

private fun formatDueDate(date: LocalDate): String =
  "${MonthAbbreviations[date.month.ordinal]} ${date.day}, ${date.year}"

private fun initialsFrom(name: String): String =
  name.trim().split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .map { it.first().uppercaseChar() }
    .joinToString("")
