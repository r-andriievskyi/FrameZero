package com.frame.zero.feature.home.tab.dashboard

data class DashboardUi(
  val displayName: String,
  val stats: DashboardStatsUi,
  val myTasks: List<DashboardTaskUi>
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
  val dueLabel: String?,
  val dueUrgency: DueUrgency
)
