package com.frame.zero.feature.home.tab.productions

import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.feature.home.testing.FakeProductionsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

@OptIn(ExperimentalCoroutinesApi::class)
class ProductionsTabViewModelTest {
  @Test
  fun `initial state has null selectedFilter`() =
    runTest {
      val viewModel = makeViewModel(this)

      assertNull(viewModel.state.value.selectedFilter)
    }

  @Test
  fun `onFilterSelected updates selectedFilter`() =
    runTest {
      val viewModel = makeViewModel(this)

      viewModel.onFilterSelected(ProductionPhase.PRODUCTION)

      assertEquals(ProductionPhase.PRODUCTION, viewModel.state.value.selectedFilter)
    }

  @Test
  fun `onFilterSelected with the same phase does not emit a new state`() =
    runTest {
      val viewModel = makeViewModel(this)
      viewModel.onFilterSelected(ProductionPhase.PRODUCTION)
      val before = viewModel.state.value

      viewModel.onFilterSelected(ProductionPhase.PRODUCTION)

      assertSame(before, viewModel.state.value)
    }

  @Test
  fun `onFilterSelected with null clears an existing filter`() =
    runTest {
      val viewModel = makeViewModel(this)
      viewModel.onFilterSelected(ProductionPhase.POST_PRODUCTION)

      viewModel.onFilterSelected(null)

      assertNull(viewModel.state.value.selectedFilter)
    }

  @Test
  fun `productions subscribes to repository with the initial null filter`() =
    runTest {
      val repo = FakeProductionsRepository()
      val viewModel = makeViewModel(this, repo)

      val job = launch { viewModel.productions.collect {} }
      runCurrent()

      assertEquals(listOf<ProductionPhase?>(null), repo.observeCalls)
      job.cancel()
      viewModel.onDestroy()
    }

  @Test
  fun `changing the filter re-subscribes to repository with the new phase`() =
    runTest {
      val repo = FakeProductionsRepository()
      val viewModel = makeViewModel(this, repo)

      val job = launch { viewModel.productions.collect {} }
      runCurrent()
      viewModel.onFilterSelected(ProductionPhase.PRODUCTION)
      runCurrent()

      assertEquals(listOf(null, ProductionPhase.PRODUCTION), repo.observeCalls)
      job.cancel()
      viewModel.onDestroy()
    }

  @Test
  fun `selecting the same filter twice does not re-subscribe the productions flow`() =
    runTest {
      val repo = FakeProductionsRepository()
      val viewModel = makeViewModel(this, repo)

      val job = launch { viewModel.productions.collect {} }
      runCurrent()
      viewModel.onFilterSelected(ProductionPhase.PRODUCTION)
      runCurrent()
      viewModel.onFilterSelected(ProductionPhase.PRODUCTION)
      runCurrent()

      assertEquals(listOf(null, ProductionPhase.PRODUCTION), repo.observeCalls)
      job.cancel()
      viewModel.onDestroy()
    }

  @Test
  fun `onDestroy cancels the cached scope so filter changes stop reaching the repository`() =
    runTest {
      val repo = FakeProductionsRepository()
      val viewModel = makeViewModel(this, repo)

      val job = launch { viewModel.productions.collect {} }
      runCurrent()
      val callsBeforeDestroy = repo.observeCalls.size

      viewModel.onDestroy()
      viewModel.onFilterSelected(ProductionPhase.PRODUCTION)
      runCurrent()

      assertEquals(callsBeforeDestroy, repo.observeCalls.size)
      job.cancel()
    }

  private fun makeViewModel(
    scope: TestScope,
    repository: FakeProductionsRepository = FakeProductionsRepository()
  ): ProductionsTabViewModel = ProductionsTabViewModel(
    productionsRepository = repository,
    dispatcher = StandardTestDispatcher(scope.testScheduler)
  )
}
