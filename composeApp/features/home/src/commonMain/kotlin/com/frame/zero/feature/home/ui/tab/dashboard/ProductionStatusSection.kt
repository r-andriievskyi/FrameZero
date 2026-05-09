package com.frame.zero.feature.home.ui.tab.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.feature.home.tab.dashboard.DashboardProductionUi
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.days_left
import framezero.composeapp.features.home.generated.resources.production_status_all_projects
import framezero.composeapp.features.home.generated.resources.production_status_title
import org.jetbrains.compose.resources.stringResource

private val PhaseIndicatorWidth = 4.dp
private val PhaseIndicatorHeight = 40.dp

@Composable
internal fun ProductionStatusSection(productions: List<DashboardProductionUi>) {
  SectionHeader(
    title = stringResource(Res.string.production_status_title),
    actionLabel = stringResource(Res.string.production_status_all_projects)
  )
  VerticalSpacer(AppTheme.spacingSystem.space8)
  productions.forEach { production ->
    ProductionCard(production = production)
    VerticalSpacer(AppTheme.spacingSystem.space8)
  }
}

@Composable
private fun ProductionCard(production: DashboardProductionUi) {
  val accentColor = accentColorFor(production.phase)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
      .background(AppTheme.colorSystem.cardBackground)
      .padding(AppTheme.spacingSystem.space16),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .width(PhaseIndicatorWidth)
        .height(PhaseIndicatorHeight)
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
        .background(accentColor)
    )
    Spacer(modifier = Modifier.width(AppTheme.spacingSystem.space8))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = production.title,
        style = AppTheme.typographySystem.titleSmall,
        color = AppTheme.colorSystem.textPrimary
      )
      VerticalSpacer(AppTheme.spacingSystem.space2)
      Text(
        text = production.phase.displayLabel(),
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted
      )
    }
    Column(horizontalAlignment = Alignment.End) {
      Text(
        text = "${production.progressPercent}%",
        style = AppTheme.typographySystem.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = accentColor
      )
      VerticalSpacer(AppTheme.spacingSystem.space2)
      Text(
        text = stringResource(Res.string.days_left, production.daysLeft),
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted
      )
    }
  }
}

@Composable
internal fun accentColorFor(phase: ProductionPhase): Color =
  when (phase) {
    ProductionPhase.PRODUCTION -> AppTheme.colorSystem.productionText
    ProductionPhase.PRE_PRODUCTION -> AppTheme.colorSystem.preProductionText
    ProductionPhase.POST_PRODUCTION -> AppTheme.colorSystem.postProductionText
    ProductionPhase.DEVELOPMENT -> AppTheme.colorSystem.developmentText
    ProductionPhase.DISTRIBUTION -> AppTheme.colorSystem.distributionText
  }

private fun ProductionPhase.displayLabel(): String =
  name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

@Preview
@Composable
private fun ProductionStatusSectionPreview() {
  AppTheme(darkTheme = true) {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      ProductionStatusSection(
        productions = listOf(
          DashboardProductionUi(
            id = "1",
            title = "Echoes of Silence",
            phase = ProductionPhase.PRODUCTION,
            progressPercent = 68,
            daysLeft = 24
          ),
          DashboardProductionUi(
            id = "2",
            title = "Neon Wolves",
            phase = ProductionPhase.PRE_PRODUCTION,
            progressPercent = 34,
            daysLeft = 61
          )
        )
      )
    }
  }
}
