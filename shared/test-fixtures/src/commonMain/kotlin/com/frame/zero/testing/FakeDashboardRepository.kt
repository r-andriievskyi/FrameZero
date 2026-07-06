package com.frame.zero.testing

import com.frame.zero.domain.dashboard.Dashboard
import com.frame.zero.domain.dashboard.DashboardStats
import com.frame.zero.repository.dashboard.DashboardRepository

class FakeDashboardRepository(
  private val dashboard: Dashboard =
    Dashboard(
      displayName = "",
      stats = DashboardStats(activeProjects = 0, openTasks = 0),
      myTasks = emptyList()
    ),
  private val throws: Throwable? = null
) : DashboardRepository {
  var getDashboardCalls: Int = 0
    private set

  override suspend fun getDashboard(): Dashboard {
    getDashboardCalls++
    throws?.let { throw it }
    return dashboard
  }
}
