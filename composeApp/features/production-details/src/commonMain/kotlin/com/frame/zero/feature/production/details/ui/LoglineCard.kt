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
import com.frame.zero.shared.design_system.AppTheme
import framezero.composeapp.features.production_details.generated.resources.Res
import framezero.composeapp.features.production_details.generated.resources.overall_progress
import framezero.composeapp.features.production_details.generated.resources.stat_budget
import framezero.composeapp.features.production_details.generated.resources.stat_days_left
import framezero.composeapp.features.production_details.generated.resources.stat_members
import org.jetbrains.compose.resources.stringResource
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.production.Genre
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

private val ProgressBarHeight = 6.dp

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
      .background(AppTheme.colorSystem.cardBackground, RoundedCornerShape(AppTheme.radiusSystem.radius16))
      .border(
        AppTheme.borderSystem.hairline,
        AppTheme.colorSystem.border,
        RoundedCornerShape(AppTheme.radiusSystem.radius16)
      )
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
        thickness = AppTheme.borderSystem.hairline,
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
        text = stringResource(Res.string.overall_progress),
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
        label = stringResource(Res.string.stat_members),
        modifier = Modifier.weight(1f)
      )
      StatItem(
        value = "${detail.daysLeft}d",
        label = stringResource(Res.string.stat_days_left),
        modifier = Modifier.weight(1f)
      )
      StatItem(
        value = formatBudget(detail.budgetCents),
        label = stringResource(Res.string.stat_budget),
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
        width = AppTheme.borderSystem.hairline,
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

private val PreviewInstant = Instant.fromEpochMilliseconds(0L)

@LightDarkPreview
@Composable
private fun LoglineCardPreview() {
  AppTheme {
    LoglineCard(
      logline = "A deaf composer rediscovers sound through the chaos of war.",
      detail = ProductionDetail(
        id = "1",
        title = "Echoes of Silence",
        genre = Genre.DRAMA,
        logline = "A deaf composer rediscovers sound through the chaos of war.",
        phase = ProductionPhase.PRODUCTION,
        progressPercent = 68,
        daysLeft = 24,
        startDate = LocalDate(2026, 2, 10),
        wrapDate = LocalDate(2026, 8, 30),
        budgetCents = 240_000_000L,
        membersCount = 12,
        keyCrew = emptyList(),
        pipeline = persistentListOf(),
        createdAt = PreviewInstant,
        updatedAt = PreviewInstant,
        viewerCrew = null
      )
    )
  }
}

@LightDarkPreview
@Composable
private fun LoglineCardNoLoglinePreview() {
  AppTheme {
    LoglineCard(
      logline = null,
      detail = ProductionDetail(
        id = "2",
        title = "Midnight Run",
        genre = Genre.ACTION,
        logline = null,
        phase = ProductionPhase.PRE_PRODUCTION,
        progressPercent = 25,
        daysLeft = 60,
        startDate = LocalDate(2026, 3, 1),
        wrapDate = LocalDate(2026, 11, 15),
        budgetCents = 500_000_000L,
        membersCount = 8,
        keyCrew = emptyList(),
        pipeline = persistentListOf(),
        createdAt = PreviewInstant,
        updatedAt = PreviewInstant,
        viewerCrew = null
      )
    )
  }
}
