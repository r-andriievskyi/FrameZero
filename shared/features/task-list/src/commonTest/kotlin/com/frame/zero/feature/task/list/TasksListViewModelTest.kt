package com.frame.zero.feature.task.list

import com.frame.zero.testing.FakeTasksRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasksListViewModelTest {
  private val testDispatcher = StandardTestDispatcher()

  private fun makeViewModel(repository: FakeTasksRepository = FakeTasksRepository()) =
    TasksListViewModel(
      productionId = "production-1",
      tasksRepository = repository,
      dispatcher = testDispatcher
    )

  @Test
  fun `viewModel constructs without error`() =
    runTest(testDispatcher) {
      makeViewModel()
    }
}
