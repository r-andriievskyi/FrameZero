package com.frame.zero.domain.dashboard

import com.frame.zero.dto.dashboard.DashboardResponse
import com.frame.zero.dto.dashboard.GreetingDto
import com.frame.zero.dto.dashboard.StatsDto
import com.frame.zero.dto.production.ProductionSummaryDto
import com.frame.zero.dto.task.TaskSummaryDto

fun DashboardResponse.toDomain(): Dashboard =
  Dashboard(
    greeting = greeting.toDomain(),
    stats = stats.toDomain(),
    myTasks = myTasks.map { it.toDomain() },
    productionStatus = productionStatus.map { it.toDomain() }
  )

fun GreetingDto.toDomain(): DashboardGreeting =
  DashboardGreeting(
    displayName = displayName,
    activeProductionsCount = activeProductionsCount,
    openTasksCount = openTasksCount
  )

fun StatsDto.toDomain(): DashboardStats = DashboardStats(activeProjects = activeProjects, openTasks = openTasks)

fun TaskSummaryDto.toDomain(): DashboardTask =
  DashboardTask(
    id = id,
    title = title,
    productionTitle = productionTitle,
    dueDate = dueDate,
    dueLabel = dueLabel,
    status = status
  )

fun ProductionSummaryDto.toDomain(): DashboardProduction =
  DashboardProduction(
    id = id,
    title = title,
    phase = phase,
    progressPercent = progressPercent,
    daysLeft = daysLeft,
    updatedAt = updatedAt
  )
