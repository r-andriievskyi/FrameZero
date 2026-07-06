package com.frame.zero.testing

import androidx.paging.PagingData
import com.frame.zero.domain.production.NewProduction
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.domain.production.ProductionMember
import com.frame.zero.repository.productions.ProductionsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Single configurable fake for [ProductionsRepository], shared across all client feature tests.
 * Construct with the data/errors a test needs; assert against the recorded call lists.
 */
class FakeProductionsRepository(
  private val detail: ProductionDetail = productionDetail(),
  private val created: ProductionDetail = detail,
  private val members: List<ProductionMember> = emptyList(),
  private val emissions: Flow<PagingData<Production>> = flowOf(PagingData.empty()),
  private val getThrows: Throwable? = null,
  private val createThrows: Throwable? = null,
  private val deleteThrows: Throwable? = null,
  private val listMembersThrows: Throwable? = null
) : ProductionsRepository {
  var observeCalls: Int = 0
    private set
  val getIds: MutableList<String> = mutableListOf()
  val deletedIds: MutableList<String> = mutableListOf()
  val listMembersCalls: MutableList<String> = mutableListOf()
  val createRequests: MutableList<NewProduction> = mutableListOf()

  override fun observeProductions(): Flow<PagingData<Production>> {
    observeCalls++
    return emissions
  }

  override suspend fun getDetails(productionId: String): ProductionDetail {
    getIds += productionId
    getThrows?.let { throw it }
    return detail
  }

  override suspend fun listMembers(productionId: String): List<ProductionMember> {
    listMembersCalls += productionId
    listMembersThrows?.let { throw it }
    return members
  }

  override suspend fun create(production: NewProduction): ProductionDetail {
    createRequests += production
    createThrows?.let { throw it }
    return created
  }

  override suspend fun delete(productionId: String) {
    deletedIds += productionId
    deleteThrows?.let { throw it }
  }
}
