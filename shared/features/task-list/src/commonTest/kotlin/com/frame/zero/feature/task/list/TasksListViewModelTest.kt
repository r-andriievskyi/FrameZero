package com.frame.zero.feature.task.list

import com.frame.zero.testing.FakeTasksRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
  fun `initial state defaults to ALL filter and DUE_DATE sort`() =
    runTest(testDispatcher) {
      val viewModel = makeViewModel()

      assertEquals(TaskListFilter.ALL, viewModel.state.value.filter)
      assertEquals(TaskListSort.DUE_DATE, viewModel.state.value.sort)
    }

  @Test
  fun `FilterChanged updates state filter`() =
    runTest(testDispatcher) {
      val viewModel = makeViewModel()

      viewModel.onIntent(TasksListIntent.FilterChanged(TaskListFilter.OPEN))

      assertEquals(TaskListFilter.OPEN, viewModel.state.value.filter)
    }

  @Test
  fun `SortChanged updates state sort`() =
    runTest(testDispatcher) {
      val viewModel = makeViewModel()

      viewModel.onIntent(TasksListIntent.SortChanged(TaskListSort.TITLE))

      assertEquals(TaskListSort.TITLE, viewModel.state.value.sort)
    }
}
