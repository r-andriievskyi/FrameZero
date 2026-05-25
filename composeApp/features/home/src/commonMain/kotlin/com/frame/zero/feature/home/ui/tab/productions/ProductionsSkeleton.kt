package com.frame.zero.feature.home.ui.tab.productions

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.widgets.VerticalSpacer

private const val SkeletonCardCount = 4
private const val PulseMinAlpha = 0.4f
private const val PulseMaxAlpha = 1f
private const val PulseDurationMillis = 900
private val TitleBarWidth = 140.dp
private val TitleBarHeight = 14.dp
private val SubtitleBarWidth = 80.dp
private val SubtitleBarHeight = 10.dp
private val BadgeWidth = 72.dp
private val BadgeHeight = 20.dp
private val ProgressLabelWidth = 90.dp
private val ProgressLabelHeight = 10.dp
private val ProgressPercentWidth = 30.dp
private val ProgressBarHeight = 6.dp
private val MetaChipWidth = 64.dp
private val MetaChipHeight = 12.dp

@Composable
internal fun ProductionsSkeleton(modifier: Modifier = Modifier) {
  val transition = rememberInfiniteTransition()
  val pulseAlpha by transition.animateFloat(
    initialValue = PulseMinAlpha,
    targetValue = PulseMaxAlpha,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = PulseDurationMillis, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    )
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .alpha(pulseAlpha),
    verticalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space16)
  ) {
    repeat(SkeletonCardCount) {
      ProductionCardSkeleton()
    }
  }
}

@Composable
private fun ProductionCardSkeleton() {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
      .background(AppTheme.colorSystem.cardBackground)
      .padding(AppTheme.spacingSystem.space16)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      Column(modifier = Modifier.weight(1f)) {
        SkeletonBar(width = TitleBarWidth, height = TitleBarHeight)
        VerticalSpacer(AppTheme.spacingSystem.space8)
        SkeletonBar(width = SubtitleBarWidth, height = SubtitleBarHeight)
      }
      SkeletonBar(width = BadgeWidth, height = BadgeHeight)
    }

    VerticalSpacer(AppTheme.spacingSystem.space16)

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      SkeletonBar(width = ProgressLabelWidth, height = ProgressLabelHeight)
      SkeletonBar(width = ProgressPercentWidth, height = ProgressLabelHeight)
    }

    VerticalSpacer(AppTheme.spacingSystem.space8)

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(ProgressBarHeight)
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
        .background(AppTheme.colorSystem.border)
    )

    VerticalSpacer(AppTheme.spacingSystem.space16)

    Row(
      horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space16),
      verticalAlignment = Alignment.CenterVertically
    ) {
      SkeletonBar(width = MetaChipWidth, height = MetaChipHeight)
      SkeletonBar(width = MetaChipWidth, height = MetaChipHeight)
    }
  }
}

@Composable
private fun SkeletonBar(
  width: Dp,
  height: Dp,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(width = width, height = height)
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
      .background(AppTheme.colorSystem.border)
  )
}

@Preview
@Composable
private fun ProductionsSkeletonPreview() {
  AppTheme(darkTheme = true) {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      ProductionsSkeleton()
    }
  }
}
