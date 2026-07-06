package com.frame.zero.shared.design_system.widgets

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.max
import kotlin.math.min

private val DefaultRefreshThreshold = 80.dp
private val DefaultMaxPullDistance = 160.dp
private val DefaultIndicatorSize = 36.dp
private val DefaultIndicatorTopPadding = 16.dp
private val DefaultIndicatorStrokeWidth = 3.dp
private const val DragRubberBand = 0.5f
private const val SpinnerSweepMin = 12f
private const val SpinnerSweepMax = 270f
private const val SpinnerPullRotationDeg = 360f
private const val MinIndicatorAlpha = 0.35f
private const val FullCircleDeg = 360f

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

object PullToRefreshDefaults {
  /**
   * Default indicator: a circular pill that floats down with the pull, rendering a sweep arc
   * that grows with [PullToRefreshState.progress] and spins while
   * [PullToRefreshState.isRefreshing] is true.
   */
  @Composable
  fun BoxScope.Indicator(
    state: PullToRefreshState,
    color: Color = AppTheme.colorSystem.accent,
    backgroundColor: Color = AppTheme.colorSystem.surfaceElevated,
    size: Dp = DefaultIndicatorSize,
    topPadding: Dp = DefaultIndicatorTopPadding,
    strokeWidth: Dp = DefaultIndicatorStrokeWidth
  ) {
    if (state.pullDistance <= 0f && !state.isRefreshing) return

    val refreshingRotation = remember { mutableFloatStateOf(0f) }
    val loopMillis = AppTheme.motionSystem.durationLoop
    LaunchedEffect(state.isRefreshing) {
      if (state.isRefreshing) {
        animate(
          initialValue = 0f,
          targetValue = FullCircleDeg,
          animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = loopMillis)
          )
        ) { value, _ -> refreshingRotation.floatValue = value }
      } else {
        refreshingRotation.floatValue = 0f
      }
    }

    val rotation = if (state.isRefreshing) {
      refreshingRotation.floatValue
    } else {
      state.progress * SpinnerPullRotationDeg
    }
    val sweep = if (state.isRefreshing) {
      SpinnerSweepMax
    } else {
      SpinnerSweepMin + (SpinnerSweepMax - SpinnerSweepMin) * state.progress
    }
    val visualProgress = if (state.isRefreshing) 1f else state.progress
    val shape = RoundedCornerShape(percent = 50)

    Box(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = topPadding)
        .size(size)
        .graphicsLayer {
          translationY = state.pullDistance - state.refreshThresholdPx
          alpha = visualProgress.coerceAtLeast(MinIndicatorAlpha)
        }
        .clip(shape)
        .background(backgroundColor, shape)
        .padding(AppTheme.spacingSystem.space4),
      contentAlignment = Alignment.Center
    ) {
      Spinner(color = color, rotation = rotation, sweep = sweep, strokeWidth = strokeWidth)
    }
  }
}

@Composable
private fun Spinner(
  color: Color,
  rotation: Float,
  sweep: Float,
  strokeWidth: Dp
) {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
    val diameter = min(size.width, size.height) - stroke.width
    val topLeft = Offset(
      x = (size.width - diameter) / 2f,
      y = (size.height - diameter) / 2f
    )
    rotate(degrees = rotation, pivot = center) {
      drawArc(
        color = color,
        startAngle = 0f,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = topLeft,
        size = Size(diameter, diameter),
        style = stroke
      )
    }
  }
}
