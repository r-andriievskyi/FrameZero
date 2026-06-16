package com.frame.zero.feature.production.details.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.frame.zero.feature.production.details.ProductionTaskUi
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.production_details.generated.resources.Res
import framezero.composeapp.features.production_details.generated.resources.tasks_add
import framezero.composeapp.features.production_details.generated.resources.tasks_empty
import framezero.composeapp.features.production_details.generated.resources.tasks_header
import org.jetbrains.compose.resources.stringResource

private val StatusDotSize = 8.dp

@Composable
internal fun TasksCard(
  tasks: List<ProductionTaskUi>,
  onAddTask: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = AppTheme.spacingSystem.space16)
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
      .background(colors.cardBackground)
      .padding(AppTheme.spacingSystem.space16)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        modifier = Modifier.semantics { heading() },
        text = stringResource(Res.string.tasks_header),
        style = AppTheme.typographySystem.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = colors.textPrimary
      )
      AddTaskButton(onClick = onAddTask)
    }

    VerticalSpacer(AppTheme.spacingSystem.space16)

    if (tasks.isEmpty()) {
      Text(
        text = stringResource(Res.string.tasks_empty),
        style = AppTheme.typographySystem.bodySmall,
        color = colors.textMuted
      )
    } else {
      tasks.forEachIndexed { index, task ->
        TaskRow(task = task)
        if (index < tasks.lastIndex) {
          VerticalSpacer(AppTheme.spacingSystem.space12)
        }
      }
    }
  }
}

@Composable
private fun TaskRow(
  task: ProductionTaskUi,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(StatusDotSize)
        .clip(CircleShape)
        .background(if (task.isDone) colors.successText else colors.accent)
    )
    HorizontalSpacer(AppTheme.spacingSystem.space12)
    Text(
      text = task.title,
      style = AppTheme.typographySystem.bodyLarge,
      color = if (task.isDone) colors.textMuted else colors.textPrimary,
      textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
      modifier = Modifier.weight(1f)
    )
    task.dueDateLabel?.let { label ->
      HorizontalSpacer(AppTheme.spacingSystem.space12)
      Text(
        text = label,
        style = AppTheme.typographySystem.bodySmall,
        color = colors.textMuted
      )
    }
  }
}

@Composable
private fun AddTaskButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  Row(
    modifier = modifier
      .clip(shape)
      .background(colors.accent, shape)
      .clickableWithRipple(color = colors.accentDim, onClick = onClick)
      .padding(
        horizontal = AppTheme.spacingSystem.space16,
        vertical = AppTheme.spacingSystem.space8
      ),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = "+",
      style = AppTheme.typographySystem.titleMedium,
      color = colors.textOnAccent
    )
    HorizontalSpacer(AppTheme.spacingSystem.space8)
    Text(
      text = stringResource(Res.string.tasks_add),
      style = AppTheme.typographySystem.labelLarge,
      color = colors.textOnAccent
    )
  }
}

@LightDarkPreview
@Composable
private fun TasksCardPreview() {
  AppTheme {
    Box(modifier = Modifier.background(AppTheme.colorSystem.background)) {
      TasksCard(
        tasks = listOf(
          ProductionTaskUi(id = "1", title = "Lock shooting schedule", dueDateLabel = "Apr 12", isDone = false),
          ProductionTaskUi(id = "2", title = "Send call sheets", dueDateLabel = null, isDone = true)
        ),
        onAddTask = {},
        modifier = Modifier.padding(vertical = AppTheme.spacingSystem.space16)
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun TasksCardEmptyPreview() {
  AppTheme {
    Box(modifier = Modifier.background(AppTheme.colorSystem.background)) {
      TasksCard(
        tasks = emptyList(),
        onAddTask = {},
        modifier = Modifier.padding(vertical = AppTheme.spacingSystem.space16)
      )
    }
  }
}
