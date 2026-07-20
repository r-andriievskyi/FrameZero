package com.frame.zero.feature.task.list.ui

import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.frame.zero.feature.task.list.TaskListItemUi
import com.frame.zero.feature.task.list.TasksListComponent
import com.frame.zero.feature.task.list.TasksListIntent

@Composable
fun TasksListScreen(component: TasksListComponent) {
  val lazyPagingItems = component.tasks.collectAsLazyPagingItems()
  TasksListContent(
    lazyPagingItems = lazyPagingItems,
    onIntent = component::onIntent,
    onTaskClick = component.onTaskClick
  )
}

// TODO: filter bar (TaskListFilterBar) + skeleton/error/empty/list states.
// Use rememberPagingListUiState(lazyPagingItems, resetKey = state.filter) to tell a filter
// switch apart from a refresh, and PagingLazyColumn for the list itself.
@Composable
internal fun TasksListContent(
  lazyPagingItems: LazyPagingItems<TaskListItemUi>,
  onIntent: (TasksListIntent) -> Unit,
  onTaskClick: (taskId: String) -> Unit
) {
}
