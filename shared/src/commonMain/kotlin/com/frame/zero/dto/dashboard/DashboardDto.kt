package com.frame.zero.dto.dashboard

import com.frame.zero.dto.production.ProductionSummaryDto
import com.frame.zero.dto.task.TaskSummaryDto
import kotlinx.serialization.Serializable

@Serializable
data class DashboardResponse(
  val greeting: GreetingDto,
  val stats: StatsDto,
  val myTasks: List<TaskSummaryDto>,
  val productionStatus: List<ProductionSummaryDto>,
)

@Serializable
data class GreetingDto(
  val displayName: String,
  val activeProductionsCount: Int,
  val openTasksCount: Int,
)

@Serializable data class StatsDto(val activeProjects: Int, val openTasks: Int)
