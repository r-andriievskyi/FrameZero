package com.discovery.playground.shared.design_system.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.discovery.playground.shared.design_system.AppTheme

private const val AppendLoadingItemKey = "append-loading"

/**
 * Snapshot of the UI-relevant load states derived from a [LazyPagingItems]. Use
 * [rememberPagingListUiState] to compute it and read the booleans to decide between skeleton,
 * empty state, and the list. [PagingLazyColumn] consumes this internally to drive
 * pull-to-refresh.
 *
 * The `resetKey` mechanism (e.g. selected filter) ensures a filter switch is treated as an
 * initial load — not as a pull-to-refresh on stale items that still belong to the previous
 * filter.
 */
@Stable
class PagingListUiState internal constructor(
  internal val itemCount: Int,
  internal val refreshState: LoadState,
  internal val isFilterTransition: Boolean
) {
  val isLoading: Boolean get() = refreshState is LoadState.Loading

  /** First load with no items to show, or load triggered by a [resetKey] change. */
  val isInitialLoad: Boolean get() = isLoading && (itemCount == 0 || isFilterTransition)

  /** Reload while items are already visible (drives pull-to-refresh). */
  val isRefreshing: Boolean get() = isLoading && itemCount > 0 && !isFilterTransition

  /** Finished loading and there is nothing to show. */
  val isEmpty: Boolean get() = !isInitialLoad && itemCount == 0
}

/**
 * Derive a [PagingListUiState] from [lazyPagingItems]. Pass a [resetKey] (e.g. the selected
 * filter) that, when it changes, should be treated as an initial load rather than a refresh —
 * the helper tracks which key the currently visible snapshot belongs to.
 */
@Composable
fun rememberPagingListUiState(
  lazyPagingItems: LazyPagingItems<*>,
  resetKey: Any? = null
): PagingListUiState {
  val refreshState = lazyPagingItems.loadState.refresh
  var snapshotKey by remember { mutableStateOf(resetKey) }
  LaunchedEffect(refreshState) {
    if (refreshState is LoadState.NotLoading) snapshotKey = resetKey
  }
  return PagingListUiState(
    itemCount = lazyPagingItems.itemCount,
    refreshState = refreshState,
    isFilterTransition = snapshotKey != resetKey
  )
}

/**
 * A `LazyColumn` wired to a [LazyPagingItems] source with pull-to-refresh and an append-loading
 * indicator baked in. The widget derives `isRefreshing` from the paging load state — callers
 * only pass the items (and optionally a [resetKey] to suppress refresh during filter changes).
 *
 * For skeleton / empty state decisions, hoist the state via [rememberPagingListUiState] and
 * pass it back in via [state]; otherwise the widget creates its own.
 *
 * The append-loading indicator is inserted automatically while [LoadState.Loading] is active
 * on the append direction.
 */
@Composable
fun <T : Any> PagingLazyColumn(
  lazyPagingItems: LazyPagingItems<T>,
  modifier: Modifier = Modifier,
  resetKey: Any? = null,
  state: PagingListUiState = rememberPagingListUiState(lazyPagingItems, resetKey),
  listState: LazyListState = rememberLazyListState(),
  contentPadding: PaddingValues = PaddingValues(0.dp),
  verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(AppTheme.spacingSystem.space16),
  itemKey: ((item: T) -> Any)? = null,
  itemContent: @Composable (T) -> Unit
) {
  PullToRefreshBox(
    isRefreshing = state.isRefreshing,
    onRefresh = lazyPagingItems::refresh,
    modifier = modifier
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      state = listState,
      contentPadding = contentPadding,
      verticalArrangement = verticalArrangement
    ) {
      items(
        count = lazyPagingItems.itemCount,
        key = itemKey?.let { lazyPagingItems.itemKey(it) }
      ) { index ->
        val item = lazyPagingItems[index] ?: return@items
        itemContent(item)
      }
      if (lazyPagingItems.loadState.append is LoadState.Loading) {
        item(key = AppendLoadingItemKey) { AppendLoadingIndicator() }
      }
    }
  }
}

@Composable
private fun AppendLoadingIndicator() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(AppTheme.spacingSystem.space16),
    contentAlignment = Alignment.Center
  ) {
    CircularProgressIndicator(color = AppTheme.colorSystem.accent)
  }
}
