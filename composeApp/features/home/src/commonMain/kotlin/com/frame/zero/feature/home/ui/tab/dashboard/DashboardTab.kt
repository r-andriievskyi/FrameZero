package com.frame.zero.feature.home.ui.tab.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.feature.home.tab.dashboard.DashboardStatsUi
import com.frame.zero.feature.home.tab.dashboard.DashboardTabComponent
import com.frame.zero.feature.home.tab.dashboard.DashboardTaskUi
import com.frame.zero.feature.home.tab.dashboard.DashboardUi
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.greeting_good_morning
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardTab(component: DashboardTabComponent) {
  LaunchedEffect(Unit) { component.onAppeared() }
  val state by component.state.collectAsState()
  DashboardContent(dashboard = state.dashboard, onTaskClick = component.onTaskClick)
}

@Composable
internal fun DashboardContent(
  dashboard: DashboardUi?,
  onTaskClick: (taskId: String) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .verticalScroll(rememberScrollState())
      .padding(
        horizontal = AppTheme.spacingSystem.space16,
        vertical = AppTheme.spacingSystem.space24
      )
  ) {
    if (dashboard != null) {
      Text(
        text = stringResource(Res.string.greeting_good_morning, dashboard.displayName),
        style = AppTheme.typographySystem.displayMedium,
        color = AppTheme.colorSystem.textPrimary
      )
      VerticalSpacer(AppTheme.spacingSystem.space16)
      StatsRow(stats = dashboard.stats)
      if (dashboard.myTasks.isNotEmpty()) {
        VerticalSpacer(AppTheme.spacingSystem.space24)
        MyTasksSection(tasks = dashboard.myTasks, onTaskClick = onTaskClick)
      }
    }
  }
}

@Preview
@Composable
private fun DashboardContentPreview() {
  AppTheme(darkTheme = true) {
    DashboardContent(
      onTaskClick = {},
      dashboard = DashboardUi(
        displayName = "Maya",
        stats = DashboardStatsUi(activeProjects = 3, openTasks = 12),
        myTasks = listOf(
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
    )
  }
}
