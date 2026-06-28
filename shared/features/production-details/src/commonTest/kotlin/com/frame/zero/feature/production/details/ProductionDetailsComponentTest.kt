package com.frame.zero.feature.production.details

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.frame.zero.feature.production.details.domain.DeleteProductionUseCase
import com.frame.zero.feature.production.details.domain.GetProductionDetailsUseCase
import com.frame.zero.feature.production.details.domain.GetProductionTasksUseCase
import com.frame.zero.testing.FakeProductionsRepository
import com.frame.zero.testing.FakeTasksRepository
import com.frame.zero.testing.productionDetailDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Exercises the [ProductionDetailsComponent] lifecycle/callback glue that sits between the
 * Decompose host and the ViewModel: the skip-first-resume refresh, the `Deleted` event →
 * `onDeleted` callback bridge, and `requestAddTask` reading the loaded title.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductionDetailsComponentTest {
  private val mainDispatcher = StandardTestDispatcher()

  @BeforeTest
  fun setUp() = Dispatchers.setMain(mainDispatcher)

  @AfterTest
  fun tearDown() = Dispatchers.resetMain()

  @Test
  fun `first resume does not refresh tasks but a later resume does`() =
    runTest(mainDispatcher) {
      val tasksRepo = FakeTasksRepository()
      val lifecycle = LifecycleRegistry()
      makeComponent(tasksRepo = tasksRepo, lifecycle = lifecycle)
      advanceUntilIdle()
      // ViewModel's own init load is the only list call so far.
      assertEquals(listOf("p1"), tasksRepo.listedProductionIds)

      // First resume is intentionally swallowed (init already loaded), so no extra list call.
      lifecycle.resume()
      advanceUntilIdle()
      assertEquals(listOf("p1"), tasksRepo.listedProductionIds)

      // Returning to the screen (e.g. back from create-task) must refresh.
      lifecycle.pause()
      lifecycle.resume()
      advanceUntilIdle()
      assertEquals(listOf("p1", "p1"), tasksRepo.listedProductionIds)

      lifecycle.destroy()
    }

  @Test
  fun `a Deleted event invokes onDeleted with the production id`() =
    runTest(mainDispatcher) {
      val deletedIds = mutableListOf<String>()
      val component = makeComponent(onDeleted = { deletedIds += it })
      advanceUntilIdle()

      component.onIntent(ProductionDetailsIntent.DeleteConfirmed)
      advanceUntilIdle()

      assertEquals(listOf("p1"), deletedIds)
    }

  @Test
  fun `requestAddTask forwards the production id and loaded title`() =
    runTest(mainDispatcher) {
      val addTaskCalls = mutableListOf<Pair<String, String>>()
      val component = makeComponent(
        productionsRepo = FakeProductionsRepository(detail = productionDetailDto(id = "p1", title = "Pilot")),
        onAddTask = { id, title -> addTaskCalls += id to title }
      )
      advanceUntilIdle()

      component.requestAddTask()

      assertEquals(listOf("p1" to "Pilot"), addTaskCalls)
    }

  @Test
  fun `requestAddTask is a no-op before the detail has loaded`() =
    runTest(mainDispatcher) {
      val addTaskCalls = mutableListOf<Pair<String, String>>()
      // getThrows leaves state.detail null, so requestAddTask must not fire.
      val component = makeComponent(
        productionsRepo = FakeProductionsRepository(getThrows = IllegalStateException("offline")),
        onAddTask = { id, title -> addTaskCalls += id to title }
      )
      advanceUntilIdle()

      component.requestAddTask()

      assertEquals(emptyList(), addTaskCalls)
      assertNull(component.state.value.detail)
    }

  private fun TestScope.makeComponent(
    productionsRepo: FakeProductionsRepository = FakeProductionsRepository(),
    tasksRepo: FakeTasksRepository = FakeTasksRepository(),
    lifecycle: LifecycleRegistry = LifecycleRegistry(),
    onBack: () -> Unit = {},
    onDeleted: (String) -> Unit = {},
    onAddTask: (String, String) -> Unit = { _, _ -> }
  ): ProductionDetailsComponent =
    ProductionDetailsComponent(
      componentContext = DefaultComponentContext(lifecycle = lifecycle),
      productionId = "p1",
      onBack = onBack,
      onDeleted = onDeleted,
      onAddTask = onAddTask,
      viewModelFactory = { productionId ->
        ProductionDetailsViewModel(
          productionId = productionId,
          getProductionDetailsUseCase = GetProductionDetailsUseCase(productionsRepo),
          getProductionTasksUseCase = GetProductionTasksUseCase(tasksRepo),
          deleteProductionUseCase = DeleteProductionUseCase(productionsRepo),
          dispatcher = StandardTestDispatcher(testScheduler)
        )
      }
    )
}
