package com.frame.zero.feature.home.tab.productions

import app.cash.turbine.test
import com.frame.zero.testing.FakeProductionsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProductionsTabViewModelTest {
  @Test
  fun `productions subscribes to the repository on first collect`() =
    runTest {
      val repo = FakeProductionsRepository()
      val viewModel = makeViewModel(this, repo)

      // PagingData isn't comparable — this subscribes without asserting on items.
      viewModel.productions.test {
        runCurrent()
        assertEquals(1, repo.observeCalls)
        cancelAndIgnoreRemainingEvents()
      }
      viewModel.onDestroy()
    }

  @Test
  fun `onDestroy cancels the cached scope`() =
    runTest {
      val repo = FakeProductionsRepository()
      val viewModel = makeViewModel(this, repo)

      viewModel.productions.test {
        runCurrent()
        viewModel.onDestroy()
        runCurrent()

        assertEquals(1, repo.observeCalls)
        cancelAndIgnoreRemainingEvents()
      }
    }

  private fun makeViewModel(
    scope: TestScope,
    repository: FakeProductionsRepository = FakeProductionsRepository()
  ): ProductionsTabViewModel =
    ProductionsTabViewModel(
      productionsRepository = repository,
      dispatcher = StandardTestDispatcher(scope.testScheduler)
    )
}
