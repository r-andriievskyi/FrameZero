package com.frame.zero.domain.task

import kotlinx.datetime.LocalDate

data class TaskSummary(
  val id: String,
  val title: String,
  val productionTitle: String,
  val dueDate: LocalDate?,
  val status: TaskStatus
)
