package com.frame.zero.repository.productions

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.frame.zero.database.FrameZeroDatabase
import com.frame.zero.domain.production.NewProduction
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.domain.production.ProductionMember
import com.frame.zero.domain.production.toCreateRequest
import com.frame.zero.domain.production.toProductionDetail
import com.frame.zero.domain.production.toProductionMember
import com.frame.zero.repository.productions.local.toProduction
import com.frame.zero.repository.productions.network.ProductionsApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PageSize = 5

class ProductionsRepositoryImpl(
  private val remoteApi: ProductionsApi,
  private val database: FrameZeroDatabase
) : ProductionsRepository {
  @OptIn(ExperimentalPagingApi::class)
  override fun observeProductions(): Flow<PagingData<Production>> {
    val dao = database.productionsCacheDao()
    return Pager(
      config = PagingConfig(pageSize = PageSize, enablePlaceholders = false),
      remoteMediator = ProductionsRemoteMediator(remoteApi, dao),
      pagingSourceFactory = { dao.pagingSource() }
    ).flow.map { pagingData -> pagingData.map { entity -> entity.toProduction() } }
  }

  override suspend fun getDetails(productionId: String): ProductionDetail =
    remoteApi.getDetails(productionId).toProductionDetail()

  override suspend fun listMembers(productionId: String): List<ProductionMember> =
    remoteApi.listMembers(productionId).map { it.toProductionMember() }

  override suspend fun create(production: NewProduction): ProductionDetail =
    remoteApi.create(production.toCreateRequest()).toProductionDetail()

  override suspend fun delete(productionId: String) {
    remoteApi.delete(productionId)
    database.productionsCacheDao().deleteById(productionId)
  }
}
