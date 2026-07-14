package com.frame.zero.demo.data

import androidx.paging.PagingData
import com.frame.zero.demo.DemoData
import com.frame.zero.demo.DemoDataStore
import com.frame.zero.domain.production.NewProduction
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.domain.production.ProductionMember
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.production.toProduction
import com.frame.zero.repository.productions.ProductionsRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

internal class DemoProductionsRepository(
  private val store: DemoDataStore
) : ProductionsRepository {
  override fun observeProductions(): Flow<PagingData<Production>> =
    store.productions.map { list -> PagingData.from(list.map { it.toProduction() }) }

  override suspend fun getDetails(productionId: String): ProductionDetail =
    store.getProduction(productionId) ?: error("Unknown demo production $productionId")

  override suspend fun listMembers(productionId: String): List<ProductionMember> =
    store.getProduction(productionId)?.keyCrew.orEmpty()

  override suspend fun create(production: NewProduction): ProductionDetail {
    val now = Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val id = "prod-demo-${now.toEpochMilliseconds()}"
    val members = production.crew.mapIndexed { index, member ->
      ProductionMember(
        id = "$id-m$index",
        userId = null,
        name = member.name,
        role = member.role,
        initials = DemoData.initials(member.name),
        avatarColorHex = DemoData.color(member.name.hashCode()),
        addedAt = now,
        reportsToMemberId = null
      )
    }
    val detail = ProductionDetail(
      id = id,
      title = production.title,
      genre = production.genre,
      logline = production.logline,
      phase = ProductionPhase.DEVELOPMENT,
      progressPercent = 0,
      daysLeft = today.daysUntil(production.wrapDate).coerceAtLeast(0),
      startDate = production.startDate,
      wrapDate = production.wrapDate,
      budgetCents = production.budgetCents,
      membersCount = members.size,
      keyCrew = members,
      pipeline = DemoData.pipeline(ProductionPhase.DEVELOPMENT).toImmutableList(),
      createdAt = now,
      updatedAt = now,
      viewerCrew = null
    )
    store.addProduction(detail)
    return detail
  }

  override suspend fun delete(productionId: String) = store.deleteProduction(productionId)
}
