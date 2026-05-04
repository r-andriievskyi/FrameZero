package com.frame.zero.repository.dashboard

import com.frame.zero.dto.dashboard.DashboardResponse

interface DashboardRepository {
  suspend fun getDashboard(): DashboardResponse
}
