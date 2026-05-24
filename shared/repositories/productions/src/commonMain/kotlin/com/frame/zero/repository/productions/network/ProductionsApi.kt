package com.frame.zero.repository.productions.network

import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.dto.common.PagedResponse
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.production.ProductionSummaryDto

interface ProductionsApi {
  suspend fun getAll(
    limit: Int,
    cursor: String?,
    phase: ProductionPhase?
  ): PagedResponse<ProductionSummaryDto>

  suspend fun getDetails(productionId: String): ProductionDetailDto

  suspend fun create(request: CreateProductionRequest): ProductionDetailDto

  suspend fun delete(productionId: String)
}
