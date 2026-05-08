package com.frame.zero.feature.home.ui.tab.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.dashboard.DashboardStats

@Composable
internal fun StatsRow(stats: DashboardStats) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
  ) {
    StatCard(
      modifier = Modifier.weight(1f),
      icon = "📊",
      value = stats.activeProjects.toString(),
      label = "Active Projects"
    )
    StatCard(
      modifier = Modifier.weight(1f),
      icon = "✅",
      value = stats.openTasks.toString(),
      label = "Open Tasks"
    )
  }
}

@Composable
private fun StatCard(
  modifier: Modifier = Modifier,
  icon: String,
  value: String,
  label: String
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
      .background(AppTheme.colorSystem.cardBackground)
      .padding(AppTheme.spacingSystem.space16)
  ) {
    Text(
      text = icon,
      style = AppTheme.typographySystem.titleMedium,
      modifier = Modifier.size(24.dp)
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    Text(
      text = value,
      style = AppTheme.typographySystem.displayMedium,
      color = AppTheme.colorSystem.textPrimary
    )
    VerticalSpacer(AppTheme.spacingSystem.space4)
    Text(
      text = label,
      style = AppTheme.typographySystem.bodySmall,
      color = AppTheme.colorSystem.textMuted
    )
  }
}

@Preview
@Composable
private fun StatsSectionPreview() {
  AppTheme(darkTheme = true) {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      StatsRow(stats = DashboardStats(activeProjects = 3, openTasks = 12))
    }
  }
}
