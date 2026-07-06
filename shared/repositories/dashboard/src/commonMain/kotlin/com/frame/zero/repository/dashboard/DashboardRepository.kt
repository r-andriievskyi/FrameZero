package com.frame.zero.repository.dashboard

import com.frame.zero.domain.dashboard.Dashboard

interface DashboardRepository {
  suspend fun getDashboard(): Dashboard
}
