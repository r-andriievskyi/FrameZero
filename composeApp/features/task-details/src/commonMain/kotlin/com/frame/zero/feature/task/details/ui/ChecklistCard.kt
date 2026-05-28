package com.frame.zero.feature.task.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import com.frame.zero.feature.task.details.ChecklistItem
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.task_details.generated.resources.Res
import framezero.composeapp.features.task_details.generated.resources.task_details_checklist
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChecklistCard(
  checklist: List<ChecklistItem>,
  onToggleItem: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius16)
  val completedCount = checklist.count { it.isCompleted }
  val totalCount = checklist.size
  val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(AppTheme.colorSystem.cardBackground)
      .padding(AppTheme.spacingSystem.space16)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      SectionLabel(text = stringResource(Res.string.task_details_checklist))
      Text(
        text = "$completedCount/$totalCount",
        style = AppTheme.typographySystem.titleSmall,
        color = AppTheme.colorSystem.accent
      )
    }
    VerticalSpacer(AppTheme.spacingSystem.space8)
    LinearProgressIndicator(
      progress = { progress },
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(ProgressBarHeight / 2)),
      color = AppTheme.colorSystem.accent,
      trackColor = AppTheme.colorSystem.border
    )
    VerticalSpacer(AppTheme.spacingSystem.space16)
    checklist.forEach { item ->
      ChecklistRow(
        item = item,
        onToggle = { onToggleItem(item.id) }
      )
      VerticalSpacer(AppTheme.spacingSystem.space8)
    }
  }
}

@Composable
private fun ChecklistRow(
  item: ChecklistItem,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Checkbox(
      checked = item.isCompleted,
      onCheckedChange = { onToggle() },
      colors = CheckboxDefaults.colors(
        checkedColor = AppTheme.colorSystem.accent,
        uncheckedColor = AppTheme.colorSystem.textMuted,
        checkmarkColor = AppTheme.colorSystem.textOnAccent
      )
    )
    HorizontalSpacer(AppTheme.spacingSystem.space8)
    Text(
      text = item.text,
      style = AppTheme.typographySystem.bodyMedium.let {
        if (item.isCompleted) it.copy(textDecoration = TextDecoration.LineThrough) else it
      },
      color = if (item.isCompleted) {
        AppTheme.colorSystem.textMuted
      } else {
        AppTheme.colorSystem.textPrimary
      }
    )
  }
}

@LightDarkPreview
@Composable
private fun ChecklistCardPreview() {
  AppTheme {
    ChecklistCard(
      checklist = listOf(
        ChecklistItem(id = "1", text = "Read revised pages (3\u20137)", isCompleted = true),
        ChecklistItem(id = "2", text = "Check continuity vs. Scene 9", isCompleted = true),
        ChecklistItem(id = "3", text = "Note blocking changes for DP", isCompleted = false),
        ChecklistItem(id = "4", text = "Sign off with writer", isCompleted = false)
      ),
      onToggleItem = {}
    )
  }
}

@LightDarkPreview
@Composable
private fun ChecklistCardAllCompletePreview() {
  AppTheme {
    ChecklistCard(
      checklist = listOf(
        ChecklistItem(id = "1", text = "Setup camera rig", isCompleted = true),
        ChecklistItem(id = "2", text = "Test audio levels", isCompleted = true)
      ),
      onToggleItem = {}
    )
  }
}

