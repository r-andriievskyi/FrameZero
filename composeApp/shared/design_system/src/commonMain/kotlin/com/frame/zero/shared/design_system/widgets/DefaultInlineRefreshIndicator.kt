package com.frame.zero.shared.design_system.widgets

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
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
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.generated.resources.Res
import com.frame.zero.shared.design_system.generated.resources.refresh_indicator_refreshing
import com.frame.zero.shared.design_system.generated.resources.refresh_indicator_release
import org.jetbrains.compose.resources.stringResource
import kotlin.math.min

private val InlineIndicatorIconSize = 44.dp
private val InlineIndicatorStrokeWidth = 2.dp
private const val FullCircleDeg = 360f

/**
 * Default inline refresh indicator. Shows a blue icon badge (arrow-up while pulling, spinning
 * refresh while loading) alongside a title and optional subtitle. Height is driven by the pull
 * distance so it grows with the gesture.
 *
 * Use directly as a custom [PagingLazyColumn] `refreshIndicator` lambda when you need to
 * customise the text:
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
  refreshingText: String = stringResource(Res.string.refresh_indicator_refreshing),
  releaseText: String = stringResource(Res.string.refresh_indicator_release),
  subtitle: String? = null
) {
  val density = LocalDensity.current
  val height = with(density) { pullState.pullDistance.toDp() }
  val isRefreshing = pullState.isRefreshing

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
  val loopMillis = AppTheme.motionSystem.durationLoop
  LaunchedEffect(isRefreshing) {
    if (isRefreshing) {
      animate(
        initialValue = 0f,
        targetValue = FullCircleDeg,
        animationSpec = infiniteRepeatable(
          animation = tween(durationMillis = loopMillis)
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
      val strokeWidthPx = InlineIndicatorStrokeWidth.toPx()
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
