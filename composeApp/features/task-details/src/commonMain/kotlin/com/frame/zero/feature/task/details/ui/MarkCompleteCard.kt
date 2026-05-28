package com.frame.zero.feature.task.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.frame.zero.feature.task.details.TaskStatus
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.task_details.generated.resources.Res
import framezero.composeapp.features.task_details.generated.resources.task_details_completed
import framezero.composeapp.features.task_details.generated.resources.task_details_in_progress
import framezero.composeapp.features.task_details.generated.resources.task_details_mark_complete
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MarkCompleteCard(
  status: TaskStatus,
  onToggleComplete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius16)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(AppTheme.colorSystem.cardBackground)
      .padding(AppTheme.spacingSystem.space16),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Checkbox(
      checked = status == TaskStatus.COMPLETED,
      onCheckedChange = { onToggleComplete() },
      colors = CheckboxDefaults.colors(
        checkedColor = AppTheme.colorSystem.accent,
        uncheckedColor = AppTheme.colorSystem.textMuted,
        checkmarkColor = AppTheme.colorSystem.textOnAccent
      )
    )
    HorizontalSpacer(AppTheme.spacingSystem.space8)
    Column {
      Text(
        text = stringResource(Res.string.task_details_mark_complete),
        style = AppTheme.typographySystem.titleSmall,
        color = AppTheme.colorSystem.textPrimary
      )
      VerticalSpacer(AppTheme.spacingSystem.space2)
      Text(
        text = if (status == TaskStatus.COMPLETED) {
          stringResource(Res.string.task_details_completed)
        } else {
          stringResource(Res.string.task_details_in_progress)
        },
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun MarkCompleteCardInProgressPreview() {
  AppTheme {
    MarkCompleteCard(
      status = TaskStatus.IN_PROGRESS,
      onToggleComplete = {}
    )
  }
}

@LightDarkPreview
@Composable
private fun MarkCompleteCardCompletedPreview() {
  AppTheme {
    MarkCompleteCard(
      status = TaskStatus.COMPLETED,
      onToggleComplete = {}
    )
  }
}

