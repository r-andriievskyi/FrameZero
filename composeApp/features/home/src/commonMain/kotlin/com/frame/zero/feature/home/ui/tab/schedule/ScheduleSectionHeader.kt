package com.frame.zero.feature.home.ui.tab.schedule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.ic_calendar_clock
import framezero.composeapp.features.home.generated.resources.ic_task
import framezero.composeapp.features.home.generated.resources.schedule_events_header
import framezero.composeapp.features.home.generated.resources.schedule_tasks_due_header
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ScheduleSectionHeader(
  icon: DrawableResource,
  title: String,
  count: Int,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Image(
      painter = painterResource(icon),
      contentDescription = null,
      colorFilter = ColorFilter.tint(AppTheme.colorSystem.accent)
    )
    HorizontalSpacer(AppTheme.spacingSystem.space8)
    Text(
      text = title,
      style = AppTheme.typographySystem.titleSmall,
      color = AppTheme.colorSystem.textPrimary
    )
    HorizontalSpacer(AppTheme.spacingSystem.space8)
    Text(
      text = count.toString(),
      style = AppTheme.typographySystem.bodySmall,
      color = AppTheme.colorSystem.textMuted
    )
  }
}

@LightDarkPreview
@Composable
private fun ScheduleSectionHeaderEventsPreview() {
  AppTheme {
    Box(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      ScheduleSectionHeader(
        icon = Res.drawable.ic_calendar_clock,
        title = stringResource(Res.string.schedule_events_header),
        count = 4
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun ScheduleSectionHeaderTasksPreview() {
  AppTheme {
    Box(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      ScheduleSectionHeader(
        icon = Res.drawable.ic_task,
        title = stringResource(Res.string.schedule_tasks_due_header),
        count = 1
      )
    }
  }
}

