package com.frame.zero.shared.design_system.widgets

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

private val DefaultRefreshThreshold = 80.dp
private val DefaultMaxPullDistance = 160.dp
private const val DragRubberBand = 0.5f

/**
 * Holds pull distance, derived progress, and the refreshing flag for [PullToRefreshBox]. Create
 * with [rememberPullToRefreshState] and read its public properties from a custom indicator slot
 * to drive the visual.
 */
class PullToRefreshState internal constructor(
  internal val refreshThresholdPx: Float,
  internal val maxPullDistancePx: Float
) {
  internal var pullDistancePx: Float by mutableFloatStateOf(0f)

  internal var isRefreshingInternal: Boolean by mutableStateOf(false)

  internal var onReleaseAboveThreshold: (() -> Unit)? = null

  /** Whether the host has signalled an active refresh. */
  val isRefreshing: Boolean get() = isRefreshingInternal

  /** Current pull offset in pixels — clamped to `[0, maxPullDistancePx]`. */
  val pullDistance: Float get() = pullDistancePx

  /** Pull progress, normalised against the refresh threshold and coerced to `[0f, 1f]`. */
  val progress: Float
    get() = (pullDistancePx / refreshThresholdPx).coerceIn(0f, 1f)

  /** Pull beyond the threshold, in `[0f, 1f]`. Use for overshoot / over-stretch effects. */
  val overshoot: Float
    get() {
      val extra = pullDistancePx - refreshThresholdPx
      val window = maxPullDistancePx - refreshThresholdPx
      return if (window <= 0f) 0f else (extra / window).coerceIn(0f, 1f)
    }

  internal val nestedScrollConnection: NestedScrollConnection =
    object : NestedScrollConnection {
      override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
      ): Offset {
        if (isRefreshingInternal) return Offset.Zero
        if (source != NestedScrollSource.UserInput) return Offset.Zero
        if (available.y < 0f && pullDistancePx > 0f) {
          val consume = max(available.y, -pullDistancePx)
          pullDistancePx += consume
          return Offset(0f, consume)
        }
        return Offset.Zero
      }

      override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
      ): Offset {
        if (isRefreshingInternal) return Offset.Zero
        if (source != NestedScrollSource.UserInput) return Offset.Zero
        if (available.y > 0f) {
          val resistance = 1f - (pullDistancePx / maxPullDistancePx).coerceIn(0f, 1f)
          val delta = available.y * resistance * DragRubberBand
          pullDistancePx = min(pullDistancePx + delta, maxPullDistancePx)
          return Offset(0f, available.y)
        }
        return Offset.Zero
      }

      override suspend fun onPreFling(available: Velocity): Velocity {
        if (isRefreshingInternal) return Velocity.Zero
        if (pullDistancePx >= refreshThresholdPx) {
          onReleaseAboveThreshold?.invoke()
          animateOffsetTo(refreshThresholdPx)
        } else if (pullDistancePx > 0f) {
          animateOffsetTo(0f)
        }
        return Velocity.Zero
      }
    }

  internal suspend fun animateOffsetTo(target: Float) {
    animate(
      initialValue = pullDistancePx,
      targetValue = target,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    ) { value, _ -> pullDistancePx = value }
  }
}

@Composable
fun rememberPullToRefreshState(
  refreshThreshold: Dp = DefaultRefreshThreshold,
  maxPullDistance: Dp = DefaultMaxPullDistance
): PullToRefreshState {
  val density = LocalDensity.current
  return remember(density, refreshThreshold, maxPullDistance) {
    PullToRefreshState(
      refreshThresholdPx = with(density) { refreshThreshold.toPx() },
      maxPullDistancePx = with(density) { maxPullDistance.toPx() }
    )
  }
}
