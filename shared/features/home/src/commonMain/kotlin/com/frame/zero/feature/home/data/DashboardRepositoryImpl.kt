package com.frame.zero.feature.home.data

import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.domain.dashboard.Dashboard
import com.frame.zero.domain.dashboard.toDomain
import com.frame.zero.dto.dashboard.DashboardResponse
import com.frame.zero.repository.dashboard.DashboardRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class DashboardRepositoryImpl(
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig
) : DashboardRepository {
  override suspend fun getDashboard(): Dashboard =
    httpClient.get("${networkConfig.baseUrl}/api/v1/dashboard").body<DashboardResponse>().toDomain()
}
