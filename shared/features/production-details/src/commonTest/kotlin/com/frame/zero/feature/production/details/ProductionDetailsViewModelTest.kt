package com.frame.zero.feature.production.details

import androidx.paging.PagingData
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.frame.zero.domain.production.NewProduction
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.domain.production.ProductionMember
import com.frame.zero.domain.task.TaskStatus
import com.frame.zero.domain.task.TaskSummary
import com.frame.zero.feature.production.details.domain.DeleteProductionUseCase
import com.frame.zero.feature.production.details.domain.GetProductionDetailsUseCase
import com.frame.zero.feature.production.details.domain.GetProductionTasksUseCase
import com.frame.zero.testing.FakeProductionsRepository
import com.frame.zero.testing.FakeTaskUploadScheduler
import com.frame.zero.testing.FakeTasksRepository
import com.frame.zero.testing.productionDetail
import com.frame.zero.repository.productions.ProductionsRepository
import com.frame.zero.repository.tasks.TasksRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
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
      val repo = FakeProductionsRepository(detail = productionDetail(id = "p1", title = "Pilot"))
      val viewModel = makeViewModel(repo)

      viewModel.state.test {
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertFalse(state.isLoading)
        assertEquals("Pilot", assertNotNull(state.detail).title)
        assertNull(state.error)
      }
    }

  @Test
  fun `init loads and maps tasks for the production`() =
    runTest {
      val tasksRepo = FakeTasksRepository(
        tasks = listOf(
          TaskSummary(
            id = "t1",
            title = "Lock schedule",
            productionTitle = "Pilot",
            dueDate = LocalDate(2026, 4, 12),
            status = TaskStatus.OPEN
          ),
          TaskSummary(
            id = "t2",
            title = "Send call sheets",
            productionTitle = "Pilot",
            dueDate = null,
            status = TaskStatus.DONE
          )
        )
      )
      val viewModel = makeViewModel(FakeProductionsRepository(), tasksRepo)

      viewModel.state.test {
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertEquals(2, state.tasks.size)
        assertFalse(state.tasks[0].isDone)
        assertNotNull(state.tasks[0].dueDateLabel)
        assertTrue(state.tasks[1].isDone)
        assertNull(state.tasks[1].dueDateLabel)
        assertFalse(state.areTasksLoading)
      }
      assertEquals(listOf("p1"), tasksRepo.listedProductionIds)
    }

  @Test
  fun `init failure sets error`() =
    runTest {
      val repo = FakeProductionsRepository(getThrows = IOException("offline"))
      val viewModel = makeViewModel(repo)

      viewModel.state.test {
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertNull(state.detail)
        assertNotNull(state.error)
        assertFalse(state.isLoading)
      }
    }

  @Test
  fun `delete requested and dismissed toggle the dialog`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())
      viewModel.state.test {
        viewModel.onIntent(ProductionDetailsIntent.DeleteRequested)
        advanceUntilIdle()
        assertTrue(expectMostRecentItem().isDeleteDialogVisible)

        viewModel.onIntent(ProductionDetailsIntent.DeleteDismissed)
        advanceUntilIdle()
        assertFalse(expectMostRecentItem().isDeleteDialogVisible)
      }
    }

  @Test
  fun `delete confirmed emits Deleted event and clears deleting`() =
    runTest {
      val repo = FakeProductionsRepository()
      val viewModel = makeViewModel(repo)
      advanceUntilIdle()

      turbineScope {
        val state = viewModel.state.testIn(backgroundScope)
        val events = viewModel.events.testIn(backgroundScope)

        viewModel.onIntent(ProductionDetailsIntent.DeleteConfirmed)
        advanceUntilIdle()

        val settled = state.expectMostRecentItem()
        assertFalse(settled.isDeleting)
        assertFalse(settled.isDeleteDialogVisible)
        assertEquals(ProductionDetailsEvent.Deleted("p1"), events.awaitItem())

        state.cancelAndIgnoreRemainingEvents()
        events.cancelAndIgnoreRemainingEvents()
      }
      assertEquals(listOf("p1"), repo.deletedIds)
    }

  @Test
  fun `delete failure sets deleteError then dismissed clears it`() =
    runTest {
      val repo = FakeProductionsRepository(deleteThrows = IOException("offline"))
      val viewModel = makeViewModel(repo)
      viewModel.state.test {
        viewModel.onIntent(ProductionDetailsIntent.DeleteConfirmed)
        advanceUntilIdle()

        val failed = expectMostRecentItem()
        assertNotNull(failed.deleteError)
        assertFalse(failed.isDeleting)

        viewModel.onIntent(ProductionDetailsIntent.DeleteErrorDismissed)
        advanceUntilIdle()
        assertNull(expectMostRecentItem().deleteError)
      }
    }

  @Test
  fun `delete confirmed ignores re-entrant calls while in flight`() =
    runTest {
      val gate = CompletableDeferred<Unit>()
      val repo = FakeDeleteRepository(gate)
      val viewModel = makeViewModel(repo)
      advanceUntilIdle()

      viewModel.state.test {
        viewModel.onIntent(ProductionDetailsIntent.DeleteConfirmed)
        runCurrent()
        assertTrue(expectMostRecentItem().isDeleting)

        // second confirm while the first delete is suspended must be a no-op
        viewModel.onIntent(ProductionDetailsIntent.DeleteConfirmed)
        runCurrent()

        gate.complete(Unit)
        advanceUntilIdle()
        cancelAndIgnoreRemainingEvents()
      }
      assertEquals(1, repo.deleteCalls)
    }

  private fun TestScope.makeViewModel(
    repo: ProductionsRepository,
    tasksRepo: TasksRepository = FakeTasksRepository()
  ): ProductionDetailsViewModel =
    ProductionDetailsViewModel(
      productionId = "p1",
      getProductionDetailsUseCase = GetProductionDetailsUseCase(repo),
      getProductionTasksUseCase = GetProductionTasksUseCase(tasksRepo),
      deleteProductionUseCase = DeleteProductionUseCase(repo),
      pendingUploadStore = testPendingUploadStore(),
      taskUploadScheduler = FakeTaskUploadScheduler(),
      dispatcher = StandardTestDispatcher(testScheduler)
    )

  private class FakeDeleteRepository(
    private val gate: CompletableDeferred<Unit>
  ) : ProductionsRepository {
    var deleteCalls = 0

    override fun observeProductions(): Flow<PagingData<Production>> = flowOf(PagingData.empty())

    override suspend fun getDetails(productionId: String): ProductionDetail = productionDetail()

    override suspend fun listMembers(productionId: String): List<ProductionMember> = emptyList()

    override suspend fun create(production: NewProduction): ProductionDetail = error("not used")

    override suspend fun delete(productionId: String) {
      deleteCalls++
      gate.await()
    }
  }
}
