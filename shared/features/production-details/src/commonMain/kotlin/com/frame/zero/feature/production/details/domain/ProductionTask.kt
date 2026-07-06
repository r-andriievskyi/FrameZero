package com.frame.zero.feature.production.details.domain

import com.frame.zero.domain.task.TaskStatus
import com.frame.zero.domain.task.TaskSummary
import kotlinx.datetime.LocalDate

/** A task belonging to a production, as shown in the production-details tasks card. */
data class ProductionTask(
  val id: String,
  val title: String,
  val dueDate: LocalDate?,
  val isDone: Boolean
)

fun TaskSummary.toProductionTask(): ProductionTask =
  ProductionTask(
    id = id,
    title = title,
    dueDate = dueDate,
    isDone = status == TaskStatus.DONE
  )
