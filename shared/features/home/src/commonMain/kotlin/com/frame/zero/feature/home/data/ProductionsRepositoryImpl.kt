package com.frame.zero.feature.home.data

import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.dto.common.PagedResponse
import com.frame.zero.dto.production.ProductionSummaryDto
import com.frame.zero.repository.productions.ProductionsRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ProductionsRepositoryImpl(
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig,
) : ProductionsRepository {
  override suspend fun list(): PagedResponse<ProductionSummaryDto> =
    httpClient.get("${networkConfig.baseUrl}/api/v1/productions").body()
}
