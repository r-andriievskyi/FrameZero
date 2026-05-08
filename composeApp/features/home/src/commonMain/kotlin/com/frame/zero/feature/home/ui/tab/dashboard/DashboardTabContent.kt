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
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.dashboard.Dashboard
import com.frame.zero.domain.dashboard.DashboardGreeting
import com.frame.zero.domain.dashboard.DashboardProduction
import com.frame.zero.domain.dashboard.DashboardStats
import com.frame.zero.domain.dashboard.DashboardTask
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.dto.production.AccentColorHint
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.feature.home.tab.dashboard.DashboardTabComponent
import com.frame.zero.feature.home.tab.dashboard.DashboardTabState
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

@Composable
fun DashboardTabContent(component: DashboardTabComponent) {
  LaunchedEffect(Unit) { component.onAppeared() }
  val state by component.state.collectAsState()
  DashboardContent(state = state)
}

@Composable
private fun DashboardContent(state: DashboardTabState) {
  val dashboard = state.dashboard
  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .background(AppTheme.colorSystem.background)
        .verticalScroll(rememberScrollState())
        .padding(
          horizontal = AppTheme.spacingSystem.space16,
          vertical = AppTheme.spacingSystem.space24
        )
  ) {
    if (dashboard != null) {
      GreetingSection(greeting = dashboard.greeting)
      VerticalSpacer(AppTheme.spacingSystem.space16)
      StatsRow(stats = dashboard.stats)
      VerticalSpacer(AppTheme.spacingSystem.space24)
      MyTasksSection(tasks = dashboard.myTasks)
      VerticalSpacer(AppTheme.spacingSystem.space24)
      ProductionStatusSection(productions = dashboard.productionStatus)
    } else if (state.userName != null) {
      Text(
        text = "Hello, ${state.userName}",
        style = AppTheme.typographySystem.displayMedium,
        color = AppTheme.colorSystem.textPrimary
      )
    }
  }
}

@Preview
@Composable
private fun DashboardContentPreview() {
  AppTheme(darkTheme = true) {
    DashboardContent(
      state =
        DashboardTabState(
          isLoading = false,
          userName = "Maya",
          dashboard =
            Dashboard(
              greeting =
                DashboardGreeting(
                  displayName = "Maya",
                  activeProductionsCount = 3,
                  openTasksCount = 12
                ),
              stats = DashboardStats(activeProjects = 3, openTasks = 12),
              myTasks =
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
                ),
              productionStatus =
                listOf(
                  DashboardProduction(
                    id = "1",
                    title = "Echoes of Silence",
                    phase = ProductionPhase.PRODUCTION,
                    progressPercent = 68,
                    daysLeft = 24,
                    accentColorHint = AccentColorHint.GREEN,
                    updatedAt = Instant.fromEpochSeconds(0)
                  ),
                  DashboardProduction(
                    id = "2",
                    title = "Neon Wolves",
                    phase = ProductionPhase.PRE_PRODUCTION,
                    progressPercent = 34,
                    daysLeft = 61,
                    accentColorHint = AccentColorHint.PURPLE,
                    updatedAt = Instant.fromEpochSeconds(0)
                  )
                )
            )
        )
    )
  }
}
