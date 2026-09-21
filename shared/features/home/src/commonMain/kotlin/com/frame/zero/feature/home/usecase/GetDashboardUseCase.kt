package com.frame.zero.feature.home.usecase

import com.frame.zero.domain.NoParamsUseCase
import com.frame.zero.domain.dashboard.Dashboard
import com.frame.zero.repository.dashboard.DashboardRepository
import dev.zacsweers.metro.Inject

@Inject
class GetDashboardUseCase(
  private val dashboardRepository: DashboardRepository
) : NoParamsUseCase<Dashboard>() {
  override suspend fun execute(): Dashboard = dashboardRepository.getDashboard()
}
