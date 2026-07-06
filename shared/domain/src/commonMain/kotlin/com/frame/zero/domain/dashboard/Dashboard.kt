package com.frame.zero.domain.dashboard

import com.frame.zero.domain.task.TaskStatus
import kotlinx.datetime.LocalDate

data class Dashboard(
  val displayName: String,
  val stats: DashboardStats,
  val myTasks: List<DashboardTask>
)

data class DashboardStats(
  val activeProjects: Int,
  val openTasks: Int
)

data class DashboardTask(
  val id: String,
  val title: String,
  val productionTitle: String,
  val dueDate: LocalDate?,
  val status: TaskStatus
)
