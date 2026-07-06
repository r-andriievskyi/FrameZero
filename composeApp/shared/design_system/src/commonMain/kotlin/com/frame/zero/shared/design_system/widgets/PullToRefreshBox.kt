package com.frame.zero.shared.design_system.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * A pull-to-refresh container with a fully replaceable [indicator] slot. Wrap any scrollable
 * [content] (lazy lists, verticalScroll columns, …) — the nested-scroll connection drives
 * [state]. On release above the threshold, [onRefresh] fires; while the host keeps
 * [isRefreshing] true the indicator stays parked. When [isRefreshing] flips back to false the
 * indicator springs back.
 *
 * The default indicator is a circular spinner tinted with `AppTheme.colorSystem.accent`. Pass
 * your own composable to fully replace it — it receives the live [PullToRefreshState].
 */
@Composable
fun PullToRefreshBox(
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
  state: PullToRefreshState = rememberPullToRefreshState(),
  indicator: @Composable BoxScope.(PullToRefreshState) -> Unit = { s ->
    with(PullToRefreshDefaults) { Indicator(state = s) }
  },
  content: @Composable BoxScope.() -> Unit
) {
  state.isRefreshingInternal = isRefreshing
  state.onReleaseAboveThreshold = onRefresh

  LaunchedEffect(state) {
    snapshotFlow { state.isRefreshing }
      .distinctUntilChanged()
      .collect { refreshing ->
        state.animateOffsetTo(if (refreshing) state.refreshThresholdPx else 0f)
      }
  }

  Box(modifier = modifier.nestedScroll(state.nestedScrollConnection)) {
    content()
    indicator(state)
  }
}
