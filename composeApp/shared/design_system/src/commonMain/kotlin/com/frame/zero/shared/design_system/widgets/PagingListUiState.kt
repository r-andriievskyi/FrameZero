package com.frame.zero.shared.design_system.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

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

  /** Refresh failed (e.g. offline). */
  val isError: Boolean get() = refreshState is LoadState.Error

  /** First load with no items to show. Suppressed during a filter transition so the list
   *  doesn't flicker into the skeleton when Room has cached items for the new key. */
  val isInitialLoad: Boolean get() = isLoading && itemCount == 0 && !isFilterTransition

  /** Refresh failed with no cached items to fall back on — drives the full-screen error. */
  val isInitialError: Boolean get() = isError && itemCount == 0 && !isFilterTransition

  /** Reload while items are already visible (drives pull-to-refresh). */
  val isRefreshing: Boolean get() = isLoading && itemCount > 0 && !isFilterTransition

  /** Finished loading and there is nothing to show. Excludes the error case so a failed
   *  refresh surfaces an error state, not a misleading "empty" state. */
  val isEmpty: Boolean get() = !isLoading && !isError && !isFilterTransition && itemCount == 0
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
