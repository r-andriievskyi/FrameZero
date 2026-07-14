package com.frame.zero.demo.data

import com.frame.zero.core.session.UserCache
import com.frame.zero.demo.DemoData
import com.frame.zero.demo.DemoDataStore
import com.frame.zero.domain.dashboard.Dashboard
import com.frame.zero.domain.dashboard.DashboardStats
import com.frame.zero.domain.dashboard.DashboardTask
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.task.TaskStatus
import com.frame.zero.repository.dashboard.DashboardRepository

internal class DemoDashboardRepository(
  private val store: DemoDataStore,
  private val userCache: UserCache
) : DashboardRepository {
  override suspend fun getDashboard(): Dashboard {
    val productions = store.productions.value
    val tasks = store.tasks.value
    val user = userCache.load() ?: DemoData.defaultUser
    val activeProjects = productions.count { it.phase != ProductionPhase.ARCHIVED }
    val openTasks = tasks.count { it.status == TaskStatus.OPEN }
    val myTasks = tasks
      .filter { it.assigneeUserId == DemoData.USER_ID && it.status == TaskStatus.OPEN }
      .sortedBy { it.dueDate }
      .map {
        DashboardTask(
          id = it.id,
          title = it.title,
          productionTitle = it.productionTitle,
          dueDate = it.dueDate,
          status = it.status
        )
      }
    return Dashboard(
      displayName = user.firstName.ifBlank { user.email.substringBefore('@') },
      stats = DashboardStats(activeProjects = activeProjects, openTasks = openTasks),
      myTasks = myTasks
    )
  }
}
