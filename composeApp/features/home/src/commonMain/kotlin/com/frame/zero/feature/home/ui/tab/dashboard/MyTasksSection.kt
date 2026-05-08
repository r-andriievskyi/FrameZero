package com.frame.zero.feature.home.ui.tab.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.dashboard.DashboardTask
import com.frame.zero.dto.task.TaskStatus
import kotlinx.datetime.LocalDate

@Composable
internal fun MyTasksSection(tasks: List<DashboardTask>) {
  SectionHeader(title = "My Tasks", actionLabel = "See all")
  VerticalSpacer(AppTheme.spacingSystem.space8)
  tasks.forEach { task ->
    TaskCard(task = task)
    VerticalSpacer(AppTheme.spacingSystem.space8)
  }
}

@Composable
private fun TaskCard(task: DashboardTask) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
        .background(AppTheme.colorSystem.cardBackground)
        .padding(AppTheme.spacingSystem.space16),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = task.title,
        style = AppTheme.typographySystem.titleSmall,
        color = AppTheme.colorSystem.textPrimary
      )
      VerticalSpacer(AppTheme.spacingSystem.space4)
      Row {
        Text(
          text = task.productionTitle,
          style = AppTheme.typographySystem.bodySmall,
          color = AppTheme.colorSystem.textMuted
        )
        val dueLabel = task.dueLabel
        if (dueLabel != null) {
          Text(
            text = " · ",
            style = AppTheme.typographySystem.bodySmall,
            color = AppTheme.colorSystem.textMuted
          )
          val dueLabelColor =
            when {
              dueLabel.equals("Today", ignoreCase = true) -> AppTheme.colorSystem.errorText
              dueLabel.equals("Tomorrow", ignoreCase = true) -> AppTheme.colorSystem.warningText
              else -> AppTheme.colorSystem.textMuted
            }
          Text(text = dueLabel, style = AppTheme.typographySystem.bodySmall, color = dueLabelColor)
        }
      }
    }
    Text(
      text = "›",
      style = AppTheme.typographySystem.titleLarge,
      color = AppTheme.colorSystem.textMuted
    )
  }
}

@Preview
@Composable
private fun MyTasksSectionPreview() {
  AppTheme(darkTheme = true) {
    Column(
      modifier =
        Modifier
          .background(AppTheme.colorSystem.background)
          .padding(AppTheme.spacingSystem.space16)
    ) {
      MyTasksSection(
        tasks =
          listOf(
            DashboardTask(
              id = "1",
              title = "Review Scene 12 script revisions",
              productionTitle = "Echoes of Silence",
              dueDate = LocalDate(2026, 5, 4),
              dueLabel = "Today",
              status = TaskStatus.OPEN
            ),
            DashboardTask(
              id = "2",
              title = "Confirm exterior shooting locations",
              productionTitle = "Neon Wolves",
              dueDate = LocalDate(2026, 5, 5),
              dueLabel = "Tomorrow",
              status = TaskStatus.OPEN
            ),
            DashboardTask(
              id = "3",
              title = "Approve final color grade",
              productionTitle = "The Last Frame",
              dueDate = LocalDate(2026, 4, 28),
              dueLabel = "Apr 28",
              status = TaskStatus.OPEN
            )
          )
      )
    }
  }
}
