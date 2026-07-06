package com.frame.zero.shared.design_system.widgets

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import kotlin.math.min

private val DefaultIndicatorSize = 36.dp
private val DefaultIndicatorTopPadding = 16.dp
private val DefaultIndicatorStrokeWidth = 3.dp
private const val SpinnerSweepMin = 12f
private const val SpinnerSweepMax = 270f
private const val SpinnerPullRotationDeg = 360f
private const val MinIndicatorAlpha = 0.35f
private const val FullCircleDeg = 360f

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
