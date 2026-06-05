package com.frame.zero.feature.home.tab.productions

import com.frame.zero.feature.home.testing.FakeProductionsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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

      val job = launch { viewModel.productions.collect {} }
      runCurrent()

      assertEquals(1, repo.observeCalls)
      job.cancel()
      viewModel.onDestroy()
    }

  @Test
  fun `onDestroy cancels the cached scope`() =
    runTest {
      val repo = FakeProductionsRepository()
      val viewModel = makeViewModel(this, repo)

      val job = launch { viewModel.productions.collect {} }
      runCurrent()
      viewModel.onDestroy()
      runCurrent()

      assertEquals(1, repo.observeCalls)
      job.cancel()
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
