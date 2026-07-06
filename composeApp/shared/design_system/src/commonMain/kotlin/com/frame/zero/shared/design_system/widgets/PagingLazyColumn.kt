package com.frame.zero.shared.design_system.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.generated.resources.Res
import com.frame.zero.shared.design_system.generated.resources.action_retry
import com.frame.zero.shared.design_system.generated.resources.error_network_message
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource

private const val AppendLoadingItemKey = "append-loading"
private const val AppendErrorItemKey = "append-error"
private const val RefreshIndicatorItemKey = "inline-refresh-indicator"

/**
 * A `LazyColumn` wired to a [LazyPagingItems] source with an inline pull-to-refresh indicator
 * and an append-loading indicator baked in. The inline indicator slides in between the top of the
 * list and the first item.
 *
 * For skeleton / empty state decisions, hoist the state via [rememberPagingListUiState] and
 * pass it back in via [state]; otherwise the widget creates its own.
 *
 * The append-loading indicator is inserted automatically while [LoadState.Loading] is active
 * on the append direction.
 *
 * @param refreshIndicator composable rendered inline while pulling/refreshing. Receives the
 *   [PullToRefreshState] so custom indicators can read progress, pull distance, and
 *   isRefreshing. The composable's height should be driven by [PullToRefreshState.pullDistance]
 *   (convert with `LocalDensity.current.run { pullDistance.toDp() }`) so it grows with the
 *   pull gesture. Pass `null` to disable the inline indicator entirely.
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
  refreshIndicator: (@Composable (PullToRefreshState) -> Unit)? = { pullState ->
    DefaultInlineRefreshIndicator(pullState = pullState)
  },
  itemKey: ((item: T) -> Any)? = null,
  itemContent: @Composable (T) -> Unit
) {
  val pullState = rememberPullToRefreshState()
  pullState.isRefreshingInternal = state.isRefreshing
  pullState.onReleaseAboveThreshold = lazyPagingItems::refresh

  LaunchedEffect(pullState) {
    snapshotFlow { pullState.isRefreshing }
      .distinctUntilChanged()
      .collect { refreshing ->
        pullState.animateOffsetTo(
          if (refreshing) pullState.refreshThresholdPx else 0f
        )
      }
  }

  Box(modifier = modifier.nestedScroll(pullState.nestedScrollConnection)) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      state = listState,
      contentPadding = contentPadding,
      verticalArrangement = verticalArrangement
    ) {
      if (refreshIndicator != null &&
        (pullState.pullDistance > 0f || pullState.isRefreshing)
      ) {
        item(key = RefreshIndicatorItemKey) {
          refreshIndicator(pullState)
        }
      }
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
      if (lazyPagingItems.loadState.append is LoadState.Error) {
        item(key = AppendErrorItemKey) {
          AppendErrorRetry(onRetry = lazyPagingItems::retry)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppendLoadingIndicator() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(AppTheme.spacingSystem.space16),
    contentAlignment = Alignment.Center
  ) {
    LoadingIndicator(color = AppTheme.colorSystem.accent)
  }
}

/** Footer shown when paging the next page fails (e.g. offline). Lets the user retry
 *  the append without reloading the whole list. */
@Composable
private fun AppendErrorRetry(onRetry: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(AppTheme.spacingSystem.space16),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = stringResource(Res.string.error_network_message),
      style = AppTheme.typographySystem.bodySmall,
      color = AppTheme.colorSystem.textSecondary
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    CtaButton(
      text = stringResource(Res.string.action_retry),
      onClick = onRetry
    )
  }
}
