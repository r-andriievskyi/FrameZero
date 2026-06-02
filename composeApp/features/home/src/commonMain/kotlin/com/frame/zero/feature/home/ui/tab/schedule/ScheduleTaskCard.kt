package com.frame.zero.feature.home.ui.tab.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.shared.design_system.widgets.rememberRoundedCornerShape
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.schedule_priority_high
import framezero.composeapp.features.home.generated.resources.schedule_priority_low
import framezero.composeapp.features.home.generated.resources.schedule_priority_medium
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ScheduleTaskCard(
  title: String,
  dueLabel: String?,
  productionTitle: String,
  priority: TaskPriority?,
  modifier: Modifier = Modifier
) {
  val shape = rememberRoundedCornerShape(AppTheme.radiusSystem.radius16)
  val colorSystem = AppTheme.colorSystem
  val spacingSystem = AppTheme.spacingSystem
  val typographySystem = AppTheme.typographySystem
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .border(
        width = AppTheme.borderSystem.hairline,
        color = colorSystem.border,
        shape = shape
      )
      .background(colorSystem.cardBackground)
      .padding(spacingSystem.space16),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = typographySystem.titleMedium,
        color = colorSystem.textPrimary
      )
      VerticalSpacer(spacingSystem.space4)
      if (dueLabel != null) {
        Text(
          text = dueLabel,
          style = typographySystem.bodySmall,
          color = colorSystem.textMuted
        )
        VerticalSpacer(spacingSystem.space4)
      }
      Text(
        text = productionTitle,
        style = typographySystem.bodySmall,
        color = colorSystem.textMuted
      )
    }

    if (priority != null) {
      HorizontalSpacer(spacingSystem.space8)
      PriorityBadge(priority = priority)
    }
  }
}

@Composable
private fun PriorityBadge(
  priority: TaskPriority,
  modifier: Modifier = Modifier
) {
  val colors = priority.badgeColors()
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius4)
  Box(
    modifier = modifier
      .clip(shape)
      .background(colors.background)
      .padding(
        horizontal = AppTheme.spacingSystem.space8,
        vertical = AppTheme.spacingSystem.space4
      ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = priority.displayLabel(),
      style = AppTheme.typographySystem.labelSmall,
      color = colors.text
    )
  }
}

@Composable
private fun TaskPriority.badgeColors(): BadgeColors =
  when (this) {
    TaskPriority.HIGH -> BadgeColors(AppTheme.colorSystem.priorityHighSurface, AppTheme.colorSystem.priorityHighText)
    TaskPriority.MEDIUM -> BadgeColors(AppTheme.colorSystem.priorityMedSurface, AppTheme.colorSystem.priorityMedText)
    TaskPriority.LOW -> BadgeColors(AppTheme.colorSystem.priorityLowSurface, AppTheme.colorSystem.priorityLowText)
  }

@Composable
private fun TaskPriority.displayLabel(): String =
  when (this) {
    TaskPriority.HIGH -> stringResource(Res.string.schedule_priority_high)
    TaskPriority.MEDIUM -> stringResource(Res.string.schedule_priority_medium)
    TaskPriority.LOW -> stringResource(Res.string.schedule_priority_low)
  }

private data class BadgeColors(
  val background: Color,
  val text: Color
)

@LightDarkPreview
@Composable
private fun ScheduleTaskCardPreview() {
  AppTheme {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      ScheduleTaskCard(
        title = "Review Scene 12 script revisions",
        dueLabel = "Due Today",
        productionTitle = "Echoes of Silence",
        priority = TaskPriority.HIGH
      )
      VerticalSpacer(AppTheme.spacingSystem.space8)
      ScheduleTaskCard(
        title = "Confirm exterior shooting locations",
        dueLabel = "Due Tomorrow",
        productionTitle = "Neon Wolves",
        priority = TaskPriority.MEDIUM
      )
      VerticalSpacer(AppTheme.spacingSystem.space8)
      ScheduleTaskCard(
        title = "Approve final color grade",
        dueLabel = null,
        productionTitle = "The Last Frame",
        priority = TaskPriority.LOW
      )
    }
  }
}
