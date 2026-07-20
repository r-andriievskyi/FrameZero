package com.frame.zero.repository.tasks.local

import com.frame.zero.database.entity.TaskSummaryEntity
import com.frame.zero.domain.task.TaskStatus
import com.frame.zero.domain.task.TaskSummary
import com.frame.zero.dto.task.TaskSummaryDto
import kotlinx.datetime.LocalDate

internal fun TaskSummaryDto.toEntity(pageOrder: Long): TaskSummaryEntity =
  TaskSummaryEntity(
    id = id,
    title = title,
    productionTitle = productionTitle,
    dueDateEpochDays = dueDate?.toEpochDays(),
    status = status.name,
    pageOrder = pageOrder
  )

internal fun TaskSummaryEntity.toDomain(): TaskSummary =
  TaskSummary(
    id = id,
    title = title,
    productionTitle = productionTitle,
    dueDate = dueDateEpochDays?.let { LocalDate.fromEpochDays(it) },
    status = TaskStatus.valueOf(status)
  )
