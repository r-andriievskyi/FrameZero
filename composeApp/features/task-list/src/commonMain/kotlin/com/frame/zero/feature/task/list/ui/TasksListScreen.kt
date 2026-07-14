package com.frame.zero.feature.task.list.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.frame.zero.feature.task.list.TaskListItemUi
import com.frame.zero.feature.task.list.TasksListComponent
import com.frame.zero.feature.task.list.TasksListIntent
import com.frame.zero.feature.task.list.TasksListState

@Composable
fun TasksListScreen(component: TasksListComponent) {
  val state by component.state.collectAsStateWithLifecycle()
  val lazyPagingItems = component.tasks.collectAsLazyPagingItems()
  TasksListContent(
    state = state,
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
  state: TasksListState,
  lazyPagingItems: LazyPagingItems<TaskListItemUi>,
  onIntent: (TasksListIntent) -> Unit,
  onTaskClick: (taskId: String) -> Unit
) {

}
