package com.frame.zero.feature.production.details

import androidx.paging.PagingData
import com.frame.zero.domain.production.Production
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.feature.production.details.domain.DeleteProductionUseCase
import com.frame.zero.feature.production.details.domain.GetProductionDetailsUseCase
import com.frame.zero.feature.production.details.testing.FakeProductionsRepository
import com.frame.zero.feature.production.details.testing.productionDetailDto
import com.frame.zero.repository.productions.ProductionsRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProductionDetailsViewModelTest {
  @Test
  fun `init loads detail and clears loading`() =
    runTest {
      val repo = FakeProductionsRepository(detail = productionDetailDto(id = "p1", title = "Pilot"))
      val viewModel = makeViewModel(repo)

      advanceUntilIdle()

      assertFalse(viewModel.state.value.isLoading)
      assertEquals("Pilot", assertNotNull(viewModel.state.value.detail).title)
      assertNull(viewModel.state.value.error)
    }

  @Test
  fun `init failure sets error`() =
    runTest {
      val repo = FakeProductionsRepository(getThrows = IOException("offline"))
      val viewModel = makeViewModel(repo)

      advanceUntilIdle()

      assertNull(viewModel.state.value.detail)
      assertNotNull(viewModel.state.value.error)
      assertFalse(viewModel.state.value.isLoading)
    }

  @Test
  fun `delete requested and dismissed toggle the dialog`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())
      advanceUntilIdle()

      viewModel.onIntent(ProductionDetailsIntent.DeleteRequested)
      assertTrue(viewModel.state.value.isDeleteDialogVisible)

      viewModel.onIntent(ProductionDetailsIntent.DeleteDismissed)
      assertFalse(viewModel.state.value.isDeleteDialogVisible)
    }

  @Test
  fun `delete confirmed emits Deleted event and clears deleting`() =
    runTest {
      val repo = FakeProductionsRepository()
      val viewModel = makeViewModel(repo)
      advanceUntilIdle()
      val events = mutableListOf<ProductionDetailsEvent>()
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        viewModel.events.collect { events += it }
      }

      viewModel.onIntent(ProductionDetailsIntent.DeleteConfirmed)
      advanceUntilIdle()

      assertFalse(viewModel.state.value.isDeleting)
      assertFalse(viewModel.state.value.isDeleteDialogVisible)
      assertEquals(listOf<ProductionDetailsEvent>(ProductionDetailsEvent.Deleted("p1")), events.toList())
      assertEquals(listOf("p1"), repo.deletedIds)
    }

  @Test
  fun `delete failure sets deleteError then dismissed clears it`() =
    runTest {
      val repo = FakeProductionsRepository(deleteThrows = IOException("offline"))
      val viewModel = makeViewModel(repo)
      advanceUntilIdle()

      viewModel.onIntent(ProductionDetailsIntent.DeleteConfirmed)
      advanceUntilIdle()

      assertNotNull(viewModel.state.value.deleteError)
      assertFalse(viewModel.state.value.isDeleting)

      viewModel.onIntent(ProductionDetailsIntent.DeleteErrorDismissed)
      assertNull(viewModel.state.value.deleteError)
    }

  @Test
  fun `delete confirmed ignores re-entrant calls while in flight`() =
    runTest {
      val gate = CompletableDeferred<Unit>()
      val repo = FakeDeleteRepository(gate)
      val viewModel = makeViewModel(repo)
      advanceUntilIdle()

      viewModel.onIntent(ProductionDetailsIntent.DeleteConfirmed)
      runCurrent()
      assertTrue(viewModel.state.value.isDeleting)

      // second confirm while the first delete is suspended must be a no-op
      viewModel.onIntent(ProductionDetailsIntent.DeleteConfirmed)
      runCurrent()

      gate.complete(Unit)
      advanceUntilIdle()

      assertEquals(1, repo.deleteCalls)
    }

  private fun TestScope.makeViewModel(repo: ProductionsRepository): ProductionDetailsViewModel =
    ProductionDetailsViewModel(
      productionId = "p1",
      getProductionDetailsUseCase = GetProductionDetailsUseCase(repo),
      deleteProductionUseCase = DeleteProductionUseCase(repo),
      dispatcher = StandardTestDispatcher(testScheduler)
    )

  private class FakeDeleteRepository(
    private val gate: CompletableDeferred<Unit>
  ) : ProductionsRepository {
    var deleteCalls = 0

    override fun observeProductions(): Flow<PagingData<Production>> = flowOf(PagingData.empty())

    override suspend fun getDetails(productionId: String): ProductionDetailDto = productionDetailDto()

    override suspend fun create(request: CreateProductionRequest): ProductionDetailDto = error("not used")

    override suspend fun delete(productionId: String) {
      deleteCalls++
      gate.await()
    }
  }
}
