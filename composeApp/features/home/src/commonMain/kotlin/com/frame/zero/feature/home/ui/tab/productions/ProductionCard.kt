package com.frame.zero.feature.home.ui.tab.productions

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.feature.home.tab.productions.ProductionUi
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.days_left
import framezero.composeapp.features.home.generated.resources.ic_calendar_clock
import framezero.composeapp.features.home.generated.resources.ic_members
import framezero.composeapp.features.home.generated.resources.members_count
import framezero.composeapp.features.home.generated.resources.projects_pipeline_progress
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val ProgressBarHeight = 6.dp

@Composable
internal fun ProductionCard(
  production: ProductionUi,
  onClick: () -> Unit = {}
) {
  val accentColor = accentColorFor(production.phase)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
      .background(AppTheme.colorSystem.cardBackground)
      .border(
        AppTheme.borderSystem.hairline,
        AppTheme.colorSystem.border,
        RoundedCornerShape(AppTheme.radiusSystem.radius16)
      )
      .clickableWithRipple(color = AppTheme.colorSystem.accentDim, onClick = onClick)
      .padding(AppTheme.spacingSystem.space16)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = production.title,
          style = AppTheme.typographySystem.titleMedium,
          color = AppTheme.colorSystem.textPrimary
        )
        VerticalSpacer(AppTheme.spacingSystem.space4)
        Text(
          text = production.genre.displayLabel(),
          style = AppTheme.typographySystem.bodySmall,
          color = AppTheme.colorSystem.textMuted
        )
      }
      PhaseBadge(phase = production.phase)
    }

    VerticalSpacer(AppTheme.spacingSystem.space16)

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = stringResource(Res.string.projects_pipeline_progress),
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted
      )
      Text(
        text = "${production.progressPercent}%",
        style = AppTheme.typographySystem.labelMedium,
        color = accentColor
      )
    }

    VerticalSpacer(AppTheme.spacingSystem.space8)

    ProgressBar(progress = production.progressPercent / 100f, color = accentColor)

    VerticalSpacer(AppTheme.spacingSystem.space16)

    Row(
      horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space16),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space4),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Image(
          painter = painterResource(Res.drawable.ic_members),
          colorFilter = ColorFilter.tint(AppTheme.colorSystem.textPrimary),
          contentDescription = null
        )
        Text(
          text = stringResource(Res.string.members_count, production.membersCount),
          style = AppTheme.typographySystem.bodySmall,
          color = AppTheme.colorSystem.textMuted
        )
      }
      Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space4),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Image(
          painter = painterResource(Res.drawable.ic_calendar_clock),
          colorFilter = ColorFilter.tint(AppTheme.colorSystem.textPrimary),
          contentDescription = null
        )
        val daysColor = if (production.daysLeft <= 7) {
          AppTheme.colorSystem.errorText
        } else {
          AppTheme.colorSystem.textMuted
        }
        Text(
          text = stringResource(Res.string.days_left, production.daysLeft),
          style = AppTheme.typographySystem.bodySmall,
          color = daysColor
        )
      }
    }
  }
}

@Composable
private fun PhaseBadge(phase: ProductionPhase) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  Box(
    modifier = Modifier
      .clip(shape)
      .background(AppTheme.colorSystem.accentSurface)
      .padding(
        horizontal = AppTheme.spacingSystem.space8,
        vertical = AppTheme.spacingSystem.space4
      ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = phase.displayLabel(),
      style = AppTheme.typographySystem.labelSmall,
      color = AppTheme.colorSystem.accentText
    )
  }
}

@Composable
private fun ProgressBar(
  progress: Float,
  color: Color
) {
  val trackColor = AppTheme.colorSystem.border
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius4)

  Box(
    modifier = Modifier
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
        .background(color)
    )
  }
}

@Composable
internal fun accentColorFor(phase: ProductionPhase): Color =
  when (phase) {
    ProductionPhase.IDEA -> AppTheme.colorSystem.textMuted
    ProductionPhase.DEVELOPMENT -> AppTheme.colorSystem.developmentText
    ProductionPhase.FINANCING -> AppTheme.colorSystem.warningText
    ProductionPhase.PRE_PRODUCTION -> AppTheme.colorSystem.preProductionText
    ProductionPhase.PRODUCTION -> AppTheme.colorSystem.productionText
    ProductionPhase.POST_PRODUCTION -> AppTheme.colorSystem.postProductionText
    ProductionPhase.MARKETING -> AppTheme.colorSystem.accentText
    ProductionPhase.DISTRIBUTION -> AppTheme.colorSystem.distributionText
    ProductionPhase.RELEASE -> AppTheme.colorSystem.successText
    ProductionPhase.ARCHIVED -> AppTheme.colorSystem.textMuted
  }

internal fun ProductionPhase.displayLabel(): String =
  name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

internal fun Genre.displayLabel(): String = name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

// ── Previews ──────────────────────────────────────────────────────────

@Preview
@Composable
private fun ProductionCardPreview() {
  AppTheme(darkTheme = true) {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      ProductionCard(
        production = ProductionUi(
          id = "1",
          title = "Echoes of Silence",
          genre = Genre.DRAMA,
          phase = ProductionPhase.PRODUCTION,
          progressPercent = 68,
          daysLeft = 24,
          membersCount = 12
        )
      )
    }
  }
}

@Preview
@Composable
private fun ProductionCardUrgentPreview() {
  AppTheme(darkTheme = true) {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      ProductionCard(
        production = ProductionUi(
          id = "3",
          title = "The Last Frame",
          genre = Genre.SCI_FI,
          phase = ProductionPhase.POST_PRODUCTION,
          progressPercent = 91,
          daysLeft = 5,
          membersCount = 6
        )
      )
    }
  }
}
