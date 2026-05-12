package com.frame.zero.feature.production.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.domain.production.ProductionPhase

private val ProgressBarHeight = 6.dp
private val CardBorderWidth = 1.dp

@Composable
internal fun LoglineCard(
  logline: String?,
  detail: ProductionDetail,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = AppTheme.spacingSystem.space16)
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
      .background(AppTheme.colorSystem.cardBackground)
      .padding(AppTheme.spacingSystem.space16)
  ) {
    if (!logline.isNullOrBlank()) {
      Text(
        text = "\"$logline\"",
        style = AppTheme.typographySystem.bodyMedium.copy(
          fontStyle = FontStyle.Italic
        ),
        color = AppTheme.colorSystem.textSecondary
      )
      VerticalSpacer(AppTheme.spacingSystem.space16)
      HorizontalDivider(
        thickness = CardBorderWidth,
        color = AppTheme.colorSystem.border
      )
      VerticalSpacer(AppTheme.spacingSystem.space16)
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "OVERALL PROGRESS",
        style = AppTheme.typographySystem.caption,
        color = AppTheme.colorSystem.textMuted
      )
      Text(
        text = "${detail.progressPercent}%",
        style = AppTheme.typographySystem.labelMedium,
        color = phaseAccentColor(detail.phase)
      )
    }
    VerticalSpacer(AppTheme.spacingSystem.space8)
    GradientProgressBar(
      progress = detail.progressPercent / 100f,
      phase = detail.phase
    )
    VerticalSpacer(AppTheme.spacingSystem.space16)

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(
        AppTheme.spacingSystem.space8
      )
    ) {
      StatItem(
        value = "${detail.membersCount}",
        label = "Members",
        modifier = Modifier.weight(1f)
      )
      StatItem(
        value = "${detail.daysLeft}d",
        label = "Days left",
        modifier = Modifier.weight(1f)
      )
      StatItem(
        value = formatBudget(detail.budgetCents),
        label = "Budget",
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun GradientProgressBar(
  progress: Float,
  phase: ProductionPhase,
  modifier: Modifier = Modifier
) {
  val phaseColor = phaseAccentColor(phase)
  val trackColor = AppTheme.colorSystem.border
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius4)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(ProgressBarHeight)
      .clip(shape)
      .background(trackColor)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
        .height(ProgressBarHeight)
        .clip(shape)
        .background(
          Brush.horizontalGradient(
            colors = listOf(phaseColor, AppTheme.colorSystem.accent)
          )
        )
    )
  }
}

@Composable
private fun StatItem(
  value: String,
  label: String,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
      .border(
        width = CardBorderWidth,
        color = AppTheme.colorSystem.cardBorder,
        shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
      )
      .padding(
        horizontal = AppTheme.spacingSystem.space8,
        vertical = AppTheme.spacingSystem.space16
      ),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = value,
      style = AppTheme.typographySystem.titleMedium.copy(
        fontWeight = FontWeight.Bold
      ),
      color = AppTheme.colorSystem.textPrimary
    )
    VerticalSpacer(AppTheme.spacingSystem.space4)
    Text(
      text = label,
      style = AppTheme.typographySystem.caption,
      color = AppTheme.colorSystem.textMuted
    )
  }
}

