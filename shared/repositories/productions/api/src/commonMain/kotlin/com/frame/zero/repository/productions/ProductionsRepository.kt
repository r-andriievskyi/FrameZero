package com.frame.zero.repository.productions

import androidx.paging.PagingData
import com.frame.zero.domain.production.NewProduction
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.domain.production.ProductionMember
import kotlinx.coroutines.flow.Flow

interface ProductionsRepository {
  fun observeProductions(): Flow<PagingData<Production>>

  suspend fun getDetails(productionId: String): ProductionDetail

  suspend fun listMembers(productionId: String): List<ProductionMember>

  suspend fun create(production: NewProduction): ProductionDetail

  suspend fun delete(productionId: String)
}
