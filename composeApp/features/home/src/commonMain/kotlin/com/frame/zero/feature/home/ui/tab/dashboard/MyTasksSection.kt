package com.frame.zero.feature.home.ui.tab.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import com.frame.zero.feature.home.tab.dashboard.DashboardTaskUi
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.ic_chevron_right
import framezero.composeapp.features.home.generated.resources.my_tasks_see_all
import framezero.composeapp.features.home.generated.resources.my_tasks_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MyTasksSection(
  tasks: List<DashboardTaskUi>,
  onTaskClick: (taskId: String) -> Unit
) {
  SectionHeader(
    title = stringResource(Res.string.my_tasks_title),
    actionLabel = stringResource(Res.string.my_tasks_see_all)
  )
  VerticalSpacer(AppTheme.spacingSystem.space8)
  tasks.forEach { task ->
    TaskCard(task = task, onClick = { onTaskClick(task.id) })
    VerticalSpacer(AppTheme.spacingSystem.space8)
  }
}

@Composable
private fun TaskCard(
  task: DashboardTaskUi,
  onClick: () -> Unit
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius16)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .clickable(onClick = onClick)
      .background(AppTheme.colorSystem.cardBackground)
      .border(AppTheme.borderSystem.hairline, AppTheme.colorSystem.border, shape)
      .padding(AppTheme.spacingSystem.space16),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = task.title,
        style = AppTheme.typographySystem.bodyMedium,
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
          HorizontalSpacer(AppTheme.spacingSystem.space8)
          val dueLabelColor = when {
            dueLabel.equals("Today", ignoreCase = true) -> AppTheme.colorSystem.errorText
            dueLabel.equals("Tomorrow", ignoreCase = true) -> AppTheme.colorSystem.warningText
            else -> AppTheme.colorSystem.textMuted
          }
          Text(text = dueLabel, style = AppTheme.typographySystem.bodySmall, color = dueLabelColor)
        }
      }
    }
    Image(
      painter = painterResource(Res.drawable.ic_chevron_right),
      colorFilter = ColorFilter.tint(AppTheme.colorSystem.textPrimary),
      contentDescription = null
    )
  }
}

@Preview
@Composable
private fun MyTasksSectionPreview() {
  AppTheme(darkTheme = true) {
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
            dueLabel = "Today"
          ),
          DashboardTaskUi(
            id = "2",
            title = "Confirm exterior shooting locations",
            productionTitle = "Neon Wolves",
            dueLabel = "Tomorrow"
          ),
          DashboardTaskUi(
            id = "3",
            title = "Approve final color grade",
            productionTitle = "The Last Frame",
            dueLabel = "Apr 28"
          )
        )
      )
    }
  }
}
