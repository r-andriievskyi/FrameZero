package com.frame.zero.repository.productions

import androidx.paging.PagingData
import com.frame.zero.domain.production.Production
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.production.ProductionMemberDto
import kotlinx.coroutines.flow.Flow

interface ProductionsRepository {
  fun observeProductions(): Flow<PagingData<Production>>

  suspend fun getDetails(productionId: String): ProductionDetailDto

  suspend fun listMembers(productionId: String): List<ProductionMemberDto>

  suspend fun create(request: CreateProductionRequest): ProductionDetailDto

  suspend fun delete(productionId: String)
}
