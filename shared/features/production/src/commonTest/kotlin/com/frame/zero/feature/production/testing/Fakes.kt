package com.frame.zero.feature.production.testing

import androidx.paging.PagingData
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.production.ProductionMemberDto
import com.frame.zero.repository.productions.ProductionsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

internal fun productionDetailDto(
  id: String = "p1",
  title: String = "Pilot",
  genre: Genre = Genre.DRAMA,
  startDate: LocalDate = LocalDate(2026, 4, 1),
  wrapDate: LocalDate = LocalDate(2026, 5, 1)
): ProductionDetailDto =
  ProductionDetailDto(
    id = id,
    title = title,
    genre = genre,
    logline = null,
    phase = ProductionPhase.IDEA,
    progressPercent = 0,
    daysLeft = 0,
    startDate = startDate,
    wrapDate = wrapDate,
    budgetCents = null,
    membersCount = 0,
    keyCrew = emptyList(),
    pipeline = emptyList(),
    createdAt = Instant.fromEpochMilliseconds(0),
    updatedAt = Instant.fromEpochMilliseconds(0)
  )

internal class FakeProductionsRepository(
  private val detail: ProductionDetailDto = productionDetailDto(),
  private val createThrows: Throwable? = null
) : ProductionsRepository {
  val createRequests: MutableList<CreateProductionRequest> = mutableListOf()

  override fun observeProductions(): Flow<PagingData<Production>> = flowOf(PagingData.empty())

  override suspend fun getDetails(productionId: String): ProductionDetailDto = error("not used")

  override suspend fun listMembers(productionId: String): List<ProductionMemberDto> = error("not used")

  override suspend fun create(request: CreateProductionRequest): ProductionDetailDto {
    createRequests += request
    createThrows?.let { throw it }
    return detail
  }

  override suspend fun delete(productionId: String): Unit = error("not used")
}
