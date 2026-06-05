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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import com.frame.zero.feature.home.tab.dashboard.DashboardTaskUi
import com.frame.zero.feature.home.tab.dashboard.DueUrgency
import com.frame.zero.feature.home.ui.tab.dashboard.DashboardTestTags
import com.frame.zero.feature.home.ui.toShortDueLabel
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.shared.design_system.widgets.rememberRoundedCornerShape
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.ic_chevron_right
import framezero.composeapp.features.home.generated.resources.my_tasks_see_all
import framezero.composeapp.features.home.generated.resources.my_tasks_title
import framezero.composeapp.features.home.generated.resources.today
import framezero.composeapp.features.home.generated.resources.tomorrow
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MyTasksSection(
  tasks: List<DashboardTaskUi>,
  onTaskClick: (taskId: String) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth().testTag(DashboardTestTags.MY_TASKS_SECTION),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = stringResource(Res.string.my_tasks_title),
      style = AppTheme.typographySystem.titleMedium,
      color = AppTheme.colorSystem.textPrimary
    )
    Text(
      text = stringResource(Res.string.my_tasks_see_all),
      style = AppTheme.typographySystem.labelMedium,
      color = AppTheme.colorSystem.accentText
    )
  }
  VerticalSpacer(AppTheme.spacingSystem.space8)
  tasks.forEach { task ->
    TaskCard(
      modifier = Modifier.testTag(DashboardTestTags.taskRow(task.id)),
      task = task,
      onClick = { onTaskClick(task.id) }
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
  }
}

@Composable
private fun TaskCard(
  task: DashboardTaskUi,
  onClick: () -> Unit,
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
      .clickableWithRipple(color = colorSystem.accentDim, onClick = onClick)
      .background(colorSystem.cardBackground)
      .border(AppTheme.borderSystem.hairline, colorSystem.border, shape)
      .padding(spacingSystem.space16),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = task.title,
        style = typographySystem.bodyMedium,
        color = colorSystem.textPrimary
      )
      VerticalSpacer(spacingSystem.space4)
      Row {
        Text(
          text = task.productionTitle,
          style = typographySystem.bodySmall,
          color = colorSystem.textMuted
        )
        val dueDate = task.dueDate
        if (dueDate != null) {
          HorizontalSpacer(spacingSystem.space8)
          val dueLabelColor = when (task.dueUrgency) {
            DueUrgency.Overdue, DueUrgency.Today -> colorSystem.errorText
            DueUrgency.Tomorrow -> colorSystem.warningText
            DueUrgency.Normal -> colorSystem.textMuted
          }
          val dueLabel = when (task.dueUrgency) {
            DueUrgency.Today -> stringResource(Res.string.today)
            DueUrgency.Tomorrow -> stringResource(Res.string.tomorrow)
            DueUrgency.Overdue, DueUrgency.Normal -> dueDate.toShortDueLabel()
          }
          Text(text = dueLabel, style = typographySystem.bodySmall, color = dueLabelColor)
        }
      }
    }
    Image(
      painter = painterResource(Res.drawable.ic_chevron_right),
      colorFilter = ColorFilter.tint(colorSystem.textPrimary),
      contentDescription = null
    )
  }
}

@LightDarkPreview
@Composable
private fun MyTasksSectionPreview() {
  AppTheme {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      MyTasksSection(
        onTaskClick = {},
        tasks = listOf(
          DashboardTaskUi(
            id = "1",
            title = "Review Scene 12 script revisions",
            productionTitle = "Echoes of Silence",
            dueDate = LocalDate(2026, 4, 27),
            dueUrgency = DueUrgency.Today
          ),
          DashboardTaskUi(
            id = "2",
            title = "Confirm exterior shooting locations",
            productionTitle = "Neon Wolves",
            dueDate = LocalDate(2026, 4, 28),
            dueUrgency = DueUrgency.Tomorrow
          ),
          DashboardTaskUi(
            id = "3",
            title = "Approve final color grade",
            productionTitle = "The Last Frame",
            dueDate = LocalDate(2026, 4, 28),
            dueUrgency = DueUrgency.Normal
          )
        )
      )
    }
  }
}
