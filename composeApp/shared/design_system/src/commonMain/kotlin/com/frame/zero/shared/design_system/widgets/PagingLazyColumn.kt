package com.frame.zero.shared.design_system.widgets

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.generated.resources.Res
import com.frame.zero.shared.design_system.generated.resources.action_retry
import com.frame.zero.shared.design_system.generated.resources.error_network_message
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.min

private const val AppendLoadingItemKey = "append-loading"
private const val AppendErrorItemKey = "append-error"
private const val RefreshIndicatorItemKey = "inline-refresh-indicator"
private val InlineIndicatorIconSize = 44.dp
private const val InlineSpinnerCycleMillis = 900
private const val FullCircleDeg = 360f

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

// ── Inline refresh indicator ───────────────────────────────────────────

/**
 * Default inline refresh indicator. Shows a blue icon badge (arrow-up while pulling, spinning
 * refresh while loading) alongside a title and optional subtitle. Height is driven by the pull
 * distance so it grows with the gesture.
 *
 * Use directly as a custom [refreshIndicator] lambda when you need to customise the text:
 * ```
 * refreshIndicator = { pullState ->
 *   DefaultInlineRefreshIndicator(
 *     pullState = pullState,
 *     refreshingText = "Refreshing productions…",
 *     subtitle = "$count productions"
 *   )
 * }
 * ```
 */
@Composable
fun DefaultInlineRefreshIndicator(
  pullState: PullToRefreshState,
  modifier: Modifier = Modifier,
  refreshingText: String = "Refreshing…",
  releaseText: String = "Release to refresh",
  subtitle: String? = null
) {
  val density = LocalDensity.current
  val height = with(density) { pullState.pullDistance.toDp() }
  val isRefreshing = pullState.isRefreshing
  val reachedThreshold = pullState.progress >= 1f

  val title = if (isRefreshing) refreshingText else releaseText

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(height)
      .clipToBounds(),
    contentAlignment = Alignment.BottomStart
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = AppTheme.spacingSystem.space8),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
    ) {
      RefreshIconBadge(isRefreshing = isRefreshing)
      Column {
        Text(
          text = title,
          style = AppTheme.typographySystem.titleSmall,
          color = AppTheme.colorSystem.textPrimary
        )
        if (subtitle != null) {
          Text(
            text = subtitle,
            style = AppTheme.typographySystem.bodySmall,
            color = AppTheme.colorSystem.textSecondary
          )
        }
      }
    }
  }
}

/**
 * Blue rounded-square badge that renders an arrow-up icon while pulling or a spinning refresh
 * icon while refreshing. All icons are drawn via [Canvas] so no image resources are needed.
 */
@Composable
private fun RefreshIconBadge(isRefreshing: Boolean) {
  val accentColor = AppTheme.colorSystem.accent
  val onAccentColor = AppTheme.colorSystem.textOnAccent

  val rotation = remember { mutableFloatStateOf(0f) }
  LaunchedEffect(isRefreshing) {
    if (isRefreshing) {
      animate(
        initialValue = 0f,
        targetValue = FullCircleDeg,
        animationSpec = infiniteRepeatable(
          animation = tween(durationMillis = InlineSpinnerCycleMillis)
        )
      ) { value, _ -> rotation.floatValue = value }
    } else {
      rotation.floatValue = 0f
    }
  }

  Box(
    modifier = Modifier
      .size(InlineIndicatorIconSize)
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
      .background(accentColor),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(InlineIndicatorIconSize / 2)) {
      val strokeWidthPx = 2.dp.toPx()
      val stroke = Stroke(
        width = strokeWidthPx,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
      )
      if (isRefreshing) {
        // Rotating refresh icon: open circle with arrow tip
        rotate(degrees = rotation.floatValue, pivot = center) {
          val radius = min(size.width, size.height) / 2f - strokeWidthPx
          val arcSize = Size(radius * 2, radius * 2)
          val arcTopLeft = Offset(center.x - radius, center.y - radius)
          drawArc(
            color = onAccentColor,
            startAngle = 0f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = stroke
          )
          // Arrow tip at the end of the arc (top-center, pointing clockwise)
          val tipX = center.x
          val tipY = center.y - radius
          val arrowLen = radius * 0.45f
          val arrowPath = Path().apply {
            moveTo(tipX - arrowLen, tipY)
            lineTo(tipX, tipY)
            lineTo(tipX, tipY + arrowLen)
          }
          drawPath(arrowPath, color = onAccentColor, style = stroke)
        }
      } else {
        // Arrow-up icon
        val cx = center.x
        val halfH = size.height * 0.45f
        val top = center.y - halfH
        val bottom = center.y + halfH
        // Vertical line
        drawLine(
          color = onAccentColor,
          start = Offset(cx, top),
          end = Offset(cx, bottom),
          strokeWidth = strokeWidthPx,
          cap = StrokeCap.Round
        )
        // Arrow head
        val headLen = size.width * 0.3f
        drawLine(
          color = onAccentColor,
          start = Offset(cx - headLen, top + headLen),
          end = Offset(cx, top),
          strokeWidth = strokeWidthPx,
          cap = StrokeCap.Round
        )
        drawLine(
          color = onAccentColor,
          start = Offset(cx + headLen, top + headLen),
          end = Offset(cx, top),
          strokeWidth = strokeWidthPx,
          cap = StrokeCap.Round
        )
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
