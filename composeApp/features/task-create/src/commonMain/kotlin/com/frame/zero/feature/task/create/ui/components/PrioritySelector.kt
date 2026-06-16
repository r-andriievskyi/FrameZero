package com.frame.zero.feature.task.create.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import framezero.composeapp.features.task_create.generated.resources.Res
import framezero.composeapp.features.task_create.generated.resources.priority_high
import framezero.composeapp.features.task_create.generated.resources.priority_low
import framezero.composeapp.features.task_create.generated.resources.priority_medium
import org.jetbrains.compose.resources.stringResource

private val OptionHeight = 48.dp

@Composable
internal fun PrioritySelector(
  selected: TaskPriority,
  onSelect: (TaskPriority) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space12)
  ) {
    PriorityOption(
      label = stringResource(Res.string.priority_high),
      isSelected = selected == TaskPriority.HIGH,
      surface = AppTheme.colorSystem.priorityHighSurface,
      content = AppTheme.colorSystem.priorityHighText,
      onClick = { onSelect(TaskPriority.HIGH) },
      modifier = Modifier.weight(1f)
    )
    PriorityOption(
      label = stringResource(Res.string.priority_medium),
      isSelected = selected == TaskPriority.MEDIUM,
      surface = AppTheme.colorSystem.priorityMedSurface,
      content = AppTheme.colorSystem.priorityMedText,
      onClick = { onSelect(TaskPriority.MEDIUM) },
      modifier = Modifier.weight(1f)
    )
    PriorityOption(
      label = stringResource(Res.string.priority_low),
      isSelected = selected == TaskPriority.LOW,
      surface = AppTheme.colorSystem.priorityLowSurface,
      content = AppTheme.colorSystem.priorityLowText,
      onClick = { onSelect(TaskPriority.LOW) },
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun PriorityOption(
  label: String,
  isSelected: Boolean,
  surface: Color,
  content: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  val background = if (isSelected) surface else colors.inputBackground
  val textColor = if (isSelected) content else colors.textPrimary
  val borderColor = if (isSelected) content else colors.border

  Box(
    modifier = modifier
      .height(OptionHeight)
      .clip(shape)
      .background(background, shape)
      .border(width = AppTheme.borderSystem.hairline, color = borderColor, shape = shape)
      .clickableWithRipple(color = colors.accentDim, onClick = onClick)
      .padding(horizontal = AppTheme.spacingSystem.space12),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = AppTheme.typographySystem.labelLarge,
      color = textColor
    )
  }
}

@LightDarkPreview
@Composable
private fun PrioritySelectorPreview() {
  AppTheme {
    PrioritySelector(
      selected = TaskPriority.MEDIUM,
      onSelect = {},
      modifier = Modifier.padding(AppTheme.spacingSystem.space16)
    )
  }
}
