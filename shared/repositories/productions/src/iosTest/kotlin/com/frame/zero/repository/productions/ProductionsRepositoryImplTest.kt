package com.frame.zero.repository.productions

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.frame.zero.database.FrameZeroDatabase
import com.frame.zero.database.ProductionEntity
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.dto.common.CursorPagedResponse
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.production.ProductionMemberDto
import com.frame.zero.dto.production.ProductionSummaryDto
import com.frame.zero.repository.productions.network.ProductionsApi
import com.frame.zero.testing.productionDetailDto
import com.frame.zero.testing.productionMemberDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Direct coverage for the offline-first reference repo's own (non-paging) logic against a real
 * in-memory Room DB: `delete` is network-first then evicts the local cache, a *failed* network
 * delete must leave the cache intact (no desync), and the detail/member/create reads pass through
 * to the API. The list reconciliation itself lives in [ProductionsRemoteMediatorTest].
 *
 * Lives in `iosTest` (not `commonTest`) because the repository logic is common but the no-arg
 * in-memory Room builder is only available off-Android — Android requires a `Context`. Running it
 * on the simulator exercises the same shared code path.
 */
class ProductionsRepositoryImplTest {
  private val database: FrameZeroDatabase =
    Room.inMemoryDatabaseBuilder<FrameZeroDatabase>()
      .setDriver(BundledSQLiteDriver())
      .setQueryCoroutineContext(Dispatchers.Default)
      .build()

  @AfterTest
  fun tearDown() = database.close()

  @Test
  fun `delete removes it from the server then evicts the cached row`() =
    runTest {
      seedCache("p1", "p2")
      val api = FakeProductionsApi()
      val repo = ProductionsRepositoryImpl(api, database)

      repo.delete("p1")

      assertEquals(listOf("p1"), api.deleteCalls)
      assertEquals(listOf("p2"), cachedIds(), "only the deleted row is evicted")
    }

  @Test
  fun `a failed server delete leaves the cache intact`() =
    runTest {
      seedCache("p1")
      val api = FakeProductionsApi(deleteError = IllegalStateException("offline"))
      val repo = ProductionsRepositoryImpl(api, database)

      assertFailsWith<IllegalStateException> { repo.delete("p1") }

      // The eviction must not run when the network call fails, or the cache desyncs from the server.
      assertEquals(listOf("p1"), cachedIds())
    }

  @Test
  fun `getDetails listMembers and create pass through to the API`() =
    runTest {
      val detail = productionDetailDto(id = "p9", title = "Feature")
      val members = listOf(productionMemberDto(id = "m1"))
      val api = FakeProductionsApi(detail = detail, members = members, created = detail)
      val repo = ProductionsRepositoryImpl(api, database)

      assertEquals(detail, repo.getDetails("p9"))
      assertEquals(members, repo.listMembers("p9"))
      assertEquals(detail, repo.create(createRequest()))
      assertEquals(listOf("p9"), api.getDetailsCalls)
      assertEquals(listOf("p9"), api.listMembersCalls)
      assertEquals(api.createCalls.single().title, "New")
    }

  private suspend fun seedCache(vararg ids: String) =
    database.productionsCacheDao().insertProductions(
      ids.mapIndexed { index, id -> productionEntity(id, pageOrder = index.toLong()) }
    )

  private suspend fun cachedIds(): List<String> {
    val result = database.productionsCacheDao().pagingSource().load(
      PagingSource.LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false)
    )
    return (result as PagingSource.LoadResult.Page).data.map { it.id }
  }

  private fun productionEntity(
    id: String,
    pageOrder: Long
  ) = ProductionEntity(
    id = id,
    title = "Title $id",
    genre = Genre.DRAMA.name,
    phase = ProductionPhase.PRODUCTION.name,
    progressPercent = 0,
    daysLeft = 0,
    membersCount = 0,
    updatedAtEpochMs = 0L,
    pageOrder = pageOrder
  )

  private fun createRequest() =
    CreateProductionRequest(
      title = "New",
      genre = Genre.DRAMA,
      startDate = LocalDate(2026, 4, 1),
      wrapDate = LocalDate(2026, 5, 1)
    )

  private class FakeProductionsApi(
    private val detail: ProductionDetailDto = productionDetailDto(),
    private val members: List<ProductionMemberDto> = emptyList(),
    private val created: ProductionDetailDto = detail,
    private val deleteError: Throwable? = null
  ) : ProductionsApi {
    val deleteCalls: MutableList<String> = mutableListOf()
    val getDetailsCalls: MutableList<String> = mutableListOf()
    val listMembersCalls: MutableList<String> = mutableListOf()
    val createCalls: MutableList<CreateProductionRequest> = mutableListOf()

    override suspend fun getAll(limit: Int, cursor: String?): CursorPagedResponse<ProductionSummaryDto> =
      error("not used")

    override suspend fun getDetails(productionId: String): ProductionDetailDto {
      getDetailsCalls += productionId
      return detail
    }

    override suspend fun listMembers(productionId: String): List<ProductionMemberDto> {
      listMembersCalls += productionId
      return members
    }

    override suspend fun create(request: CreateProductionRequest): ProductionDetailDto {
      createCalls += request
      return created
    }

    override suspend fun delete(productionId: String) {
      deleteCalls += productionId
      deleteError?.let { throw it }
    }
  }
}
