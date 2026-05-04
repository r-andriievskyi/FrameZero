package com.frame.zero.domain.dashboard

import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.dto.production.AccentColorHint
import com.frame.zero.dto.task.TaskStatus
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

data class Dashboard(
  val greeting: DashboardGreeting,
  val stats: DashboardStats,
  val myTasks: List<DashboardTask>,
  val productionStatus: List<DashboardProduction>,
)

data class DashboardGreeting(
  val displayName: String,
  val activeProductionsCount: Int,
  val openTasksCount: Int,
)

data class DashboardStats(val activeProjects: Int, val openTasks: Int)

data class DashboardTask(
  val id: String,
  val title: String,
  val productionTitle: String,
  val dueDate: LocalDate?,
  val dueLabel: String?,
  val status: TaskStatus,
)

data class DashboardProduction(
  val id: String,
  val title: String,
  val phase: ProductionPhase,
  val progressPercent: Int,
  val daysLeft: Int,
  val accentColorHint: AccentColorHint,
  val updatedAt: Instant,
)
