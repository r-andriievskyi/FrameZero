package com.frame.zero.feature.task.details.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.frame.zero.feature.task.details.TaskPriority
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview

@Composable
internal fun PriorityBadge(
  priority: TaskPriority,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, label) = when (priority) {
    TaskPriority.HIGH -> Triple(
      AppTheme.colorSystem.priorityHighSurface,
      AppTheme.colorSystem.priorityHighText,
      "High"
    )
    TaskPriority.MEDIUM -> Triple(
      AppTheme.colorSystem.priorityMedSurface,
      AppTheme.colorSystem.priorityMedText,
      "Medium"
    )
    TaskPriority.LOW -> Triple(
      AppTheme.colorSystem.priorityLowSurface,
      AppTheme.colorSystem.priorityLowText,
      "Low"
    )
  }
  Text(
    text = label,
    style = AppTheme.typographySystem.labelMedium.copy(fontWeight = FontWeight.Bold),
    color = textColor,
    modifier = modifier
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
      .background(bgColor)
      .padding(horizontal = AppTheme.spacingSystem.space8, vertical = AppTheme.spacingSystem.space4)
  )
}

@LightDarkPreview
@Composable
private fun PriorityBadgePreview() {
  AppTheme {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16),
      verticalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
    ) {
      PriorityBadge(priority = TaskPriority.HIGH)
      PriorityBadge(priority = TaskPriority.MEDIUM)
      PriorityBadge(priority = TaskPriority.LOW)
    }
  }
}
