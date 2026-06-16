package com.frame.zero.repository.productions.network

import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.dto.common.CursorPagedResponse
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.production.ProductionMemberDto
import com.frame.zero.dto.production.ProductionSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class ProductionsApiImpl(
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig
) : ProductionsApi {
  override suspend fun getAll(
    limit: Int,
    cursor: String?
  ): CursorPagedResponse<ProductionSummaryDto> =
    httpClient.get("${networkConfig.baseUrl}/api/v1/productions") {
      parameter("limit", limit)
      if (cursor != null) parameter("cursor", cursor)
    }.body()

  override suspend fun getDetails(productionId: String): ProductionDetailDto =
    httpClient.get(
      "${networkConfig.baseUrl}/api/v1/productions/$productionId"
    ).body()

  override suspend fun listMembers(productionId: String): List<ProductionMemberDto> =
    httpClient.get(
      "${networkConfig.baseUrl}/api/v1/productions/$productionId/members"
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
