package com.frame.zero.feature.home.ui.tab.dashboard.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import com.frame.zero.feature.home.tab.dashboard.DashboardStatsUi
import com.frame.zero.feature.home.ui.tab.dashboard.DashboardTestTags
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.shared.design_system.widgets.rememberRoundedCornerShape
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.ic_clapper_board
import framezero.composeapp.features.home.generated.resources.ic_task
import framezero.composeapp.features.home.generated.resources.stats_active_projects
import framezero.composeapp.features.home.generated.resources.stats_open_tasks
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StatsRow(
  stats: DashboardStatsUi,
  modifier: Modifier = Modifier,
  onTasksClick: () -> Unit = {}
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
  ) {
    StatCard(
      modifier = Modifier.weight(1f).testTag(DashboardTestTags.STAT_ACTIVE_PRODUCTIONS),
      icon = Res.drawable.ic_clapper_board,
      value = stats.activeProjects.toString(),
      label = stringResource(Res.string.stats_active_projects),
      onClick = {
        // todo
      }
    )
    StatCard(
      modifier = Modifier.weight(1f).testTag(DashboardTestTags.STAT_OPEN_TASKS),
      icon = Res.drawable.ic_task,
      value = stats.openTasks.toString(),
      label = stringResource(Res.string.stats_open_tasks),
      onClick = onTasksClick
    )
  }
}

@Composable
private fun StatCard(
  modifier: Modifier = Modifier,
  icon: DrawableResource,
  value: String,
  label: String,
  onClick: () -> Unit
) {
  val shape = rememberRoundedCornerShape(AppTheme.radiusSystem.radius16)

  val colorSystem = AppTheme.colorSystem
  val spacingSystem = AppTheme.spacingSystem
  val typographySystem = AppTheme.typographySystem

  Column(
    modifier = modifier
      .semantics(mergeDescendants = true) {}
      .clip(shape)
      .clickableWithRipple(color = colorSystem.accentDim, onClick = onClick)
      .background(colorSystem.cardBackground)
      .border(AppTheme.borderSystem.hairline, colorSystem.border, shape)
      .padding(spacingSystem.space16)
  ) {
    Image(
      painter = painterResource(icon),
      colorFilter = ColorFilter.tint(colorSystem.textPrimary),
      contentDescription = null
    )
    VerticalSpacer(spacingSystem.space8)
    Text(
      text = value,
      style = typographySystem.displayMedium,
      color = colorSystem.textPrimary
    )
    VerticalSpacer(spacingSystem.space4)
    Text(
      text = label,
      style = typographySystem.bodySmall,
      color = colorSystem.textMuted
    )
  }
}

@LightDarkPreview
@Composable
private fun StatsSectionPreview() {
  AppTheme {
    StatsRow(
      modifier = Modifier.padding(AppTheme.spacingSystem.space16),
      stats = DashboardStatsUi(activeProjects = 3, openTasks = 12)
    )
  }
}
