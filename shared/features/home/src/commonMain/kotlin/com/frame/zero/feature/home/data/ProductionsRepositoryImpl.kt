package com.frame.zero.feature.home.data

import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.dto.common.PagedResponse
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.production.ProductionSummaryDto
import com.frame.zero.repository.productions.ProductionsRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class ProductionsRepositoryImpl(
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig
) : ProductionsRepository {
  override suspend fun getAll(): PagedResponse<ProductionSummaryDto> =
    httpClient.get(
      "${networkConfig.baseUrl}/api/v1/productions"
    ).body()

  override suspend fun getDetails(productionId: String): ProductionDetailDto =
    httpClient.get(
      "${networkConfig.baseUrl}/api/v1/productions/$productionId"
    ).body()

  override suspend fun create(request: CreateProductionRequest): ProductionDetailDto =
    httpClient.post(
      "${networkConfig.baseUrl}/api/v1/productions"
    ) { setBody(request) }.body()

  override suspend fun delete(productionId: String) {
    httpClient.delete(
      "${networkConfig.baseUrl}/api/v1/productions/$productionId"
    ).body<Unit>()
  }
}
