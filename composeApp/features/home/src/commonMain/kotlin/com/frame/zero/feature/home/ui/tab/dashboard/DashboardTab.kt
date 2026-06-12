package com.frame.zero.feature.home.ui.tab.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.testTag
import com.frame.zero.feature.home.LoadErrorKind
import com.frame.zero.feature.home.tab.dashboard.DashboardStatsUi
import com.frame.zero.feature.home.tab.dashboard.DashboardTabComponent
import com.frame.zero.feature.home.tab.dashboard.DashboardTaskUi
import com.frame.zero.feature.home.tab.dashboard.DashboardUi
import com.frame.zero.feature.home.tab.dashboard.DueUrgency
import com.frame.zero.feature.home.ui.tab.dashboard.components.MyTasksSection
import com.frame.zero.feature.home.ui.tab.dashboard.components.StatsRow
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.FullScreenError
import com.frame.zero.shared.design_system.widgets.FullScreenProgress
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.error_generic_message
import framezero.composeapp.features.home.generated.resources.error_offline_message
import framezero.composeapp.features.home.generated.resources.greeting_good_morning
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardTab(component: DashboardTabComponent) {
  val state by component.state.collectAsStateWithLifecycle()
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
  ) {
    when (state.error) {
      LoadErrorKind.Network -> FullScreenError(
        message = stringResource(Res.string.error_offline_message)
      )
      LoadErrorKind.Generic -> FullScreenError(
        message = stringResource(Res.string.error_generic_message),
        onRetry = component::retry
      )
      null -> if (state.isLoading) {
        FullScreenProgress()
      } else {
        DashboardContent(dashboard = state.dashboard, onTaskClick = component.onTaskClick)
      }
    }
  }
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
        modifier = Modifier
          .testTag(DashboardTestTags.GREETING)
          .semantics { heading() },
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

@LightDarkPreview
@Composable
private fun DashboardContentPreview() {
  AppTheme {
    DashboardContent(
      onTaskClick = {},
      dashboard = DashboardUi(
        displayName = "Maya",
        stats = DashboardStatsUi(activeProjects = 3, openTasks = 12),
        myTasks = persistentListOf(
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
    )
  }
}
