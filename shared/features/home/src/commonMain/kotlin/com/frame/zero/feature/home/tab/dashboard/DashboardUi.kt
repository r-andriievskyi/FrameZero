package com.frame.zero.feature.home.tab.dashboard

import com.frame.zero.domain.dashboard.Dashboard
import com.frame.zero.domain.dashboard.DashboardStats
import com.frame.zero.domain.dashboard.DashboardTask

data class DashboardUi(
  val displayName: String,
  val stats: DashboardStatsUi,
  val myTasks: List<DashboardTaskUi>
)

data class DashboardStatsUi(
  val activeProjects: Int,
  val openTasks: Int
)

data class DashboardTaskUi(
  val id: String,
  val title: String,
  val productionTitle: String,
  val dueLabel: String?
)

fun Dashboard.toUi(): DashboardUi =
  DashboardUi(
    displayName = displayName,
    stats = stats.toUi(),
    myTasks = myTasks.map { it.toUi() }
  )

fun DashboardStats.toUi(): DashboardStatsUi = DashboardStatsUi(activeProjects = activeProjects, openTasks = openTasks)

fun DashboardTask.toUi(): DashboardTaskUi =
  DashboardTaskUi(
    id = id,
    title = title,
    productionTitle = productionTitle,
    dueLabel = dueLabel
  )
