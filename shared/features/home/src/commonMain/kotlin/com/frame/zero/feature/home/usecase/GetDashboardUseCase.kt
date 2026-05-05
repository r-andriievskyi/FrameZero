package com.frame.zero.feature.home.usecase

import com.frame.zero.domain.DomainError
import com.frame.zero.domain.NoParamsUseCase
import com.frame.zero.domain.dashboard.Dashboard
import com.frame.zero.domain.dashboard.toDomain
import com.frame.zero.repository.dashboard.DashboardRepository
import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException

class GetDashboardUseCase(
  private val dashboardRepository: DashboardRepository
) : NoParamsUseCase<Dashboard>() {
  override fun mapError(throwable: Throwable): DomainError =
    when (throwable) {
      is IOException -> DomainError.Network(throwable.message ?: "Network error")
      is ResponseException -> DomainError.Unknown(throwable.message)
      else -> DomainError.Unknown(throwable.message)
    }

  override suspend fun execute(): Dashboard = dashboardRepository.getDashboard().toDomain()
}
