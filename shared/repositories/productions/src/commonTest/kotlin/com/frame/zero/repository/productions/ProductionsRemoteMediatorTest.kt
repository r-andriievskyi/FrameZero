package com.frame.zero.repository.productions

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.dto.common.CursorPagedResponse
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.production.ProductionSummaryDto
import com.frame.zero.repository.productions.local.ALL_FILTER_KEY
import com.frame.zero.repository.productions.local.ProductionEntity
import com.frame.zero.repository.productions.local.ProductionRemoteKeyEntity
import com.frame.zero.repository.productions.local.ProductionsDao
import com.frame.zero.repository.productions.network.ProductionsApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalPagingApi::class)
class ProductionsRemoteMediatorTest {
  @Test
  fun `REFRESH writes API page into the cache and stores the next cursor`() =
    runTest {
      val api = FakeProductionsApi(page = pageOf(productionSummary("p1"), nextCursor = "cursor-2"))
      val dao = FakeProductionsDao()
      val mediator = ProductionsRemoteMediator(phase = null, remoteApi = api, dao = dao)

      val result = mediator.load(LoadType.REFRESH, emptyPagingState())

      assertTrue(result is RemoteMediator.MediatorResult.Success)
      assertEquals(false, result.endOfPaginationReached)
      assertEquals(listOf("p1"), dao.getRows().map { it.id })
      assertEquals(ALL_FILTER_KEY, dao.getRows().single().phaseFilter)
      assertEquals("cursor-2", dao.getKeys()[ALL_FILTER_KEY]?.nextCursor)
    }

  @Test
  fun `REFRESH with null nextCursor signals end of pagination`() =
    runTest {
      val api = FakeProductionsApi(page = pageOf(productionSummary("p1"), nextCursor = null))
      val dao = FakeProductionsDao()
      val mediator = ProductionsRemoteMediator(phase = null, remoteApi = api, dao = dao)

      val result = mediator.load(LoadType.REFRESH, emptyPagingState())

      assertTrue(result is RemoteMediator.MediatorResult.Success)
      assertTrue(result.endOfPaginationReached)
      assertTrue(dao.getKeys().containsKey(ALL_FILTER_KEY))
      assertEquals(null, dao.getKeys()[ALL_FILTER_KEY]?.nextCursor)
    }

  @Test
  fun `REFRESH on one filter does not touch rows belonging to other filters`() =
    runTest {
      val api = FakeProductionsApi(page = pageOf(productionSummary("p2"), nextCursor = null))
      val dao = FakeProductionsDao().apply {
        addRow(productionEntity(id = "p1-prod", filter = ProductionPhase.PRODUCTION.name))
      }
      val mediator = ProductionsRemoteMediator(phase = null, remoteApi = api, dao = dao)

      mediator.load(LoadType.REFRESH, emptyPagingState())

      assertEquals(setOf("p1-prod", "p2"), dao.getRows().map { it.id }.toSet())
    }

  @Test
  fun `APPEND with no stored remote key short-circuits without calling the API`() =
    runTest {
      val api = FakeProductionsApi(page = pageOf(nextCursor = null))
      val dao = FakeProductionsDao()
      val mediator = ProductionsRemoteMediator(phase = null, remoteApi = api, dao = dao)

      val result = mediator.load(LoadType.APPEND, emptyPagingState())

      assertTrue(result is RemoteMediator.MediatorResult.Success)
      assertTrue(result.endOfPaginationReached)
      assertEquals(0, api.getAllCalls.size)
    }

  @Test
  fun `APPEND with a stored cursor calls API and continues pageOrder from max + 1`() =
    runTest {
      val api = FakeProductionsApi(page = pageOf(productionSummary("p2"), nextCursor = "cursor-3"))
      val dao = FakeProductionsDao().apply {
        addRow(productionEntity(id = "p1", filter = ALL_FILTER_KEY))
        updateKeyValue(ALL_FILTER_KEY, ProductionRemoteKeyEntity(ALL_FILTER_KEY, nextCursor = "cursor-2"))
      }
      val mediator = ProductionsRemoteMediator(phase = null, remoteApi = api, dao = dao)

      val result = mediator.load(LoadType.APPEND, emptyPagingState())

      assertTrue(result is RemoteMediator.MediatorResult.Success)
      val call = api.getAllCalls.single()
      assertEquals("cursor-2", call.cursor)
      val appended = dao.getRows().single { it.id == "p2" }
      assertEquals(1L, appended.pageOrder)
      assertEquals("cursor-3", dao.getKeys()[ALL_FILTER_KEY]?.nextCursor)
    }

  @Test
  fun `PREPEND always short-circuits without calling the API`() =
    runTest {
      val api = FakeProductionsApi(page = pageOf(nextCursor = null))
      val mediator = ProductionsRemoteMediator(phase = null, remoteApi = api, dao = FakeProductionsDao())

      val result = mediator.load(LoadType.PREPEND, emptyPagingState())

      assertTrue(result is RemoteMediator.MediatorResult.Success)
      assertTrue(result.endOfPaginationReached)
      assertEquals(0, api.getAllCalls.size)
    }

  @Test
  fun `API failure returns Error and leaves the cache untouched`() =
    runTest {
      val boom = IOException("connection refused")
      val api = FakeProductionsApi(error = boom)
      val dao = FakeProductionsDao().apply {
        addRow(productionEntity(id = "p1", filter = ALL_FILTER_KEY))
        updateKeyValue(ALL_FILTER_KEY, ProductionRemoteKeyEntity(ALL_FILTER_KEY, nextCursor = "cursor-2"))
      }
      val mediator = ProductionsRemoteMediator(phase = null, remoteApi = api, dao = dao)

      val result = mediator.load(LoadType.REFRESH, emptyPagingState())

      assertTrue(result is RemoteMediator.MediatorResult.Error)
      assertEquals(boom, result.throwable)
      assertEquals(listOf("p1"), dao.getRows().map { it.id })
      assertEquals("cursor-2", dao.getKeys()[ALL_FILTER_KEY]?.nextCursor)
    }

  @Test
  fun `CancellationException is rethrown, not converted to Error`() =
    runTest {
      val api = FakeProductionsApi(error = CancellationException("cancelled"))
      val mediator = ProductionsRemoteMediator(phase = null, remoteApi = api, dao = FakeProductionsDao())

      assertFailsWith<CancellationException> {
        mediator.load(LoadType.REFRESH, emptyPagingState())
      }
    }

  private fun emptyPagingState(): PagingState<Int, ProductionEntity> =
    PagingState(
      pages = emptyList(),
      anchorPosition = null,
      config = PagingConfig(pageSize = 5, enablePlaceholders = false),
      leadingPlaceholderCount = 0
    )

  private fun productionSummary(id: String): ProductionSummaryDto =
    ProductionSummaryDto(
      id = id,
      title = "Title $id",
      genre = Genre.DRAMA,
      phase = ProductionPhase.PRODUCTION,
      progressPercent = 42,
      daysLeft = 7,
      membersCount = 3,
      updatedAt = Instant.fromEpochMilliseconds(1_000L)
    )

  private fun productionEntity(
    id: String,
    filter: String
  ): ProductionEntity =
    ProductionEntity(
      id = id,
      phaseFilter = filter,
      title = "Title $id",
      genre = Genre.DRAMA.name,
      phase = ProductionPhase.PRODUCTION.name,
      progressPercent = 0,
      daysLeft = 0,
      membersCount = 0,
      updatedAtEpochMs = 0L,
      pageOrder = 0L
    )

  private fun pageOf(
    vararg items: ProductionSummaryDto,
    nextCursor: String?
  ): CursorPagedResponse<ProductionSummaryDto> = CursorPagedResponse(items = items.toList(), nextCursor = nextCursor)

  private class FakeProductionsApi(
    private val page: CursorPagedResponse<ProductionSummaryDto> = CursorPagedResponse(emptyList(), null),
    private val error: Throwable? = null
  ) : ProductionsApi {
    data class GetAllCall(
      val limit: Int,
      val cursor: String?,
      val phase: ProductionPhase?
    )

    val getAllCalls: MutableList<GetAllCall> = mutableListOf()

    override suspend fun getAll(
      limit: Int,
      cursor: String?,
      phase: ProductionPhase?
    ): CursorPagedResponse<ProductionSummaryDto> {
      getAllCalls += GetAllCall(limit, cursor, phase)
      error?.let { throw it }
      return page
    }

    override suspend fun getDetails(productionId: String): ProductionDetailDto = error("not used")

    override suspend fun create(request: CreateProductionRequest): ProductionDetailDto = error("not used")

    override suspend fun delete(productionId: String): Unit = error("not used")
  }

  private class FakeProductionsDao : ProductionsDao() {
    private val rows: MutableList<ProductionEntity> = mutableListOf()
    private val keys: MutableMap<String, ProductionRemoteKeyEntity> = mutableMapOf()

    override fun pagingSource(filter: String): PagingSource<Int, ProductionEntity> = error("not used")

    override suspend fun maxPageOrder(filter: String): Long? =
      rows.filter { it.phaseFilter == filter }.maxOfOrNull { it.pageOrder }

    override suspend fun remoteKey(filter: String): ProductionRemoteKeyEntity? = keys[filter]

    override suspend fun insertProductions(entities: List<ProductionEntity>) {
      rows += entities
    }

    override suspend fun upsertRemoteKey(key: ProductionRemoteKeyEntity) {
      keys[key.phaseFilter] = key
    }

    override suspend fun deleteByFilter(filter: String) {
      rows.removeAll { it.phaseFilter == filter }
    }

    override suspend fun deleteRemoteKey(filter: String) {
      keys.remove(filter)
    }

    fun getRows(): List<ProductionEntity> = rows

    fun addRow(entity: ProductionEntity) {
      rows.add(entity)
    }

    fun updateKeyValue(
      key: String,
      value: ProductionRemoteKeyEntity
    ) {
      keys[key] = value
    }

    fun getKeys(): Map<String, ProductionRemoteKeyEntity> = keys
  }
}
