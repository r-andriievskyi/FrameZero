package com.frame.zero.feature.home.tab.dashboard

import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

data class DashboardUi(
  val displayName: String,
  val stats: DashboardStatsUi,
  val myTasks: ImmutableList<DashboardTaskUi>
)

data class DashboardStatsUi(
  val activeProjects: Int,
  val openTasks: Int
)

enum class DueUrgency {
  Overdue,
  Today,
  Tomorrow,
  Normal
}

data class DashboardTaskUi(
  val id: String,
  val title: String,
  val productionTitle: String,
  val dueDate: LocalDate?,
  val dueUrgency: DueUrgency
)
