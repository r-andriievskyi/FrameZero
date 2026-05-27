package com.frame.zero.feature.home.ui.tab.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.schedule_priority_high
import framezero.composeapp.features.home.generated.resources.schedule_priority_low
import framezero.composeapp.features.home.generated.resources.schedule_priority_medium
import org.jetbrains.compose.resources.stringResource

private val CheckboxSize = 20.dp
private val CheckboxBorderWidth = 2.dp
private val PriorityBarWidth = 4.dp
private val PriorityBarHeight = 40.dp

/**
 * A task card for the schedule timeline, showing a checkbox indicator,
 * priority accent bar, title, subtitle, and priority badge.
 */
@Composable
internal fun ScheduleTaskCard(
  title: String,
  dueLabel: String?,
  productionTitle: String,
  priority: TaskPriority?,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius16)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .border(
        width = AppTheme.spacingSystem.space2,
        color = AppTheme.colorSystem.cardBorder,
        shape = shape
      )
      .background(AppTheme.colorSystem.cardBackground)
      .padding(AppTheme.spacingSystem.space16),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Priority accent bar
    val barColor = priority.accentColor()
    Box(
      modifier = Modifier
        .size(width = PriorityBarWidth, height = PriorityBarHeight)
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
        .background(barColor)
    )

    HorizontalSpacer(AppTheme.spacingSystem.space8)

    // Checkbox placeholder (unfilled circle)
    Box(
      modifier = Modifier
        .size(CheckboxSize)
        .border(CheckboxBorderWidth, AppTheme.colorSystem.cardBorder, CircleShape)
    )

    HorizontalSpacer(AppTheme.spacingSystem.space8)

    // Title + subtitle
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = AppTheme.typographySystem.titleSmall,
        color = AppTheme.colorSystem.textPrimary
      )
      VerticalSpacer(AppTheme.spacingSystem.space4)
      Row {
        if (dueLabel != null) {
          Text(
            text = dueLabel,
            style = AppTheme.typographySystem.bodySmall,
            color = AppTheme.colorSystem.textMuted
          )
          Text(
            text = "  •  ",
            style = AppTheme.typographySystem.bodySmall,
            color = AppTheme.colorSystem.textMuted
          )
        }
        Text(
          text = productionTitle,
          style = AppTheme.typographySystem.bodySmall,
          color = AppTheme.colorSystem.textMuted
        )
      }
    }

    // Priority badge
    if (priority != null) {
      HorizontalSpacer(AppTheme.spacingSystem.space8)
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
      .background(colors.first)
      .padding(
        horizontal = AppTheme.spacingSystem.space8,
        vertical = AppTheme.spacingSystem.space4
      ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = priority.displayLabel(),
      style = AppTheme.typographySystem.labelSmall,
      color = colors.second
    )
  }
}

@Composable
private fun TaskPriority.badgeColors(): Pair<Color, Color> =
  when (this) {
    TaskPriority.HIGH -> AppTheme.colorSystem.priorityHighSurface to AppTheme.colorSystem.priorityHighText
    TaskPriority.MEDIUM -> AppTheme.colorSystem.priorityMedSurface to AppTheme.colorSystem.priorityMedText
    TaskPriority.LOW -> AppTheme.colorSystem.priorityLowSurface to AppTheme.colorSystem.priorityLowText
  }

@Composable
private fun TaskPriority?.accentColor(): Color =
  when (this) {
    TaskPriority.HIGH -> AppTheme.colorSystem.priorityHighText
    TaskPriority.MEDIUM -> AppTheme.colorSystem.priorityMedText
    TaskPriority.LOW -> AppTheme.colorSystem.priorityLowText
    null -> AppTheme.colorSystem.textMuted
  }

@Composable
private fun TaskPriority.displayLabel(): String =
  when (this) {
    TaskPriority.HIGH -> stringResource(Res.string.schedule_priority_high)
    TaskPriority.MEDIUM -> stringResource(Res.string.schedule_priority_medium)
    TaskPriority.LOW -> stringResource(Res.string.schedule_priority_low)
  }

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


