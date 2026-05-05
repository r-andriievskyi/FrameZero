package com.frame.zero.feature.home.data

import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.dto.dashboard.DashboardResponse
import com.frame.zero.repository.dashboard.DashboardRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class DashboardRepositoryImpl(
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig
) : DashboardRepository {
  override suspend fun getDashboard(): DashboardResponse =
    httpClient.get("${networkConfig.baseUrl}/api/v1/dashboard").body()
}
