package com.frame.zero.feature.home.tab.dashboard

import com.frame.zero.domain.dashboard.Dashboard
import com.frame.zero.domain.dashboard.DashboardStats
import com.frame.zero.domain.dashboard.DashboardTask
import kotlinx.datetime.LocalDate

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

fun Dashboard.toUi(resolveUrgency: (DashboardTask) -> DueUrgency): DashboardUi =
  DashboardUi(
    displayName = displayName,
    stats = stats.toUi(),
    myTasks = myTasks.map { it.toUi(resolveUrgency(it)) }
  )

fun DashboardStats.toUi(): DashboardStatsUi = DashboardStatsUi(activeProjects = activeProjects, openTasks = openTasks)

fun DashboardTask.toUi(dueUrgency: DueUrgency): DashboardTaskUi =
  DashboardTaskUi(
    id = id,
    title = title,
    productionTitle = productionTitle,
    dueLabel = formatDueLabel(dueDate, dueUrgency),
    dueUrgency = dueUrgency
  )

private fun formatDueLabel(
  dueDate: LocalDate?,
  urgency: DueUrgency
): String? {
  val date = dueDate ?: return null
  return when (urgency) {
    DueUrgency.Today -> "Today"
    DueUrgency.Tomorrow -> "Tomorrow"
    DueUrgency.Overdue, DueUrgency.Normal -> {
      val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
      "$month ${date.day}"
    }
  }
}
