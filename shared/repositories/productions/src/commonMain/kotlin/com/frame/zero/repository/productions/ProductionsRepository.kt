package com.frame.zero.repository.productions

import com.frame.zero.dto.common.PagedResponse
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.production.ProductionSummaryDto

interface ProductionsRepository {
  suspend fun getAll(): PagedResponse<ProductionSummaryDto>

  suspend fun getDetails(productionId: String): ProductionDetailDto

  suspend fun create(request: CreateProductionRequest): ProductionDetailDto
}
