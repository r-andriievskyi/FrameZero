package com.frame.zero.feature.task.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontWeight
import com.frame.zero.feature.task.details.TaskPriority
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import framezero.composeapp.features.task_details.generated.resources.Res
import framezero.composeapp.features.task_details.generated.resources.task_details_high_priority
import framezero.composeapp.features.task_details.generated.resources.task_details_low_priority
import framezero.composeapp.features.task_details.generated.resources.task_details_medium_priority
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HeaderSection(
  priority: TaskPriority,
  productionName: String,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
  ) {
    PriorityBadge(priority = priority)
    ProductionIndicator(productionName = productionName)
  }
}

@Composable
internal fun PriorityBadge(
  priority: TaskPriority,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, label) = when (priority) {
    TaskPriority.HIGH -> Triple(
      AppTheme.colorSystem.priorityHighSurface,
      AppTheme.colorSystem.priorityHighText,
      stringResource(Res.string.task_details_high_priority)
    )
    TaskPriority.MEDIUM -> Triple(
      AppTheme.colorSystem.priorityMedSurface,
      AppTheme.colorSystem.priorityMedText,
      stringResource(Res.string.task_details_medium_priority)
    )
    TaskPriority.LOW -> Triple(
      AppTheme.colorSystem.priorityLowSurface,
      AppTheme.colorSystem.priorityLowText,
      stringResource(Res.string.task_details_low_priority)
    )
  }
  Text(
    text = label,
    style = AppTheme.typographySystem.labelMedium.copy(fontWeight = FontWeight.Bold),
    color = textColor,
    modifier = modifier
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
      .background(bgColor)
      .padding(horizontal = AppTheme.spacingSystem.space8, vertical = AppTheme.spacingSystem.space4)
  )
}

@Composable
internal fun ProductionIndicator(
  productionName: String,
  modifier: Modifier = Modifier
) {
  if (productionName.isBlank()) return
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(ProductionDotSize)
        .clip(CircleShape)
        .background(AppTheme.colorSystem.successText)
    )
    HorizontalSpacer(AppTheme.spacingSystem.space4)
    Text(
      text = productionName,
      style = AppTheme.typographySystem.bodyMedium,
      color = AppTheme.colorSystem.textSecondary
    )
  }
}

@LightDarkPreview
@Composable
private fun HeaderSectionHighPriorityPreview() {
  AppTheme {
    HeaderSection(
      priority = TaskPriority.HIGH,
      productionName = "Echoes of Silence"
    )
  }
}

@LightDarkPreview
@Composable
private fun HeaderSectionMediumPriorityPreview() {
  AppTheme {
    HeaderSection(
      priority = TaskPriority.MEDIUM,
      productionName = "Summer Campaign 2026"
    )
  }
}

@LightDarkPreview
@Composable
private fun HeaderSectionLowPriorityPreview() {
  AppTheme {
    HeaderSection(
      priority = TaskPriority.LOW,
      productionName = "Behind the Lens"
    )
  }
}

