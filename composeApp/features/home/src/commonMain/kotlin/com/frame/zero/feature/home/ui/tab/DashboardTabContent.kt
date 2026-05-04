package com.frame.zero.feature.home.ui.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

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
      Modifier.fillMaxSize()
        .background(AppTheme.colorSystem.background)
        .verticalScroll(rememberScrollState())
        .padding(
          horizontal = AppTheme.spacingSystem.space16,
          vertical = AppTheme.spacingSystem.space24,
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
        color = AppTheme.colorSystem.textPrimary,
      )
    }
  }
}

// ── Greeting ──────────────────────────────────────────────────────────

@Composable
private fun GreetingSection(greeting: DashboardGreeting) {
  Text(
    text = "Good morning, ${greeting.displayName} 👋",
    style = AppTheme.typographySystem.displayMedium,
    color = AppTheme.colorSystem.textPrimary,
  )
  VerticalSpacer(AppTheme.spacingSystem.space4)
  Text(
    text =
      "${greeting.activeProductionsCount} active productions · " +
        "${greeting.openTasksCount} open tasks",
    style = AppTheme.typographySystem.bodyMedium,
    color = AppTheme.colorSystem.textSecondary,
  )
}

// ── Stats cards ───────────────────────────────────────────────────────

@Composable
private fun StatsRow(stats: DashboardStats) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8),
  ) {
    StatCard(
      modifier = Modifier.weight(1f),
      icon = "📊",
      value = stats.activeProjects.toString(),
      label = "Active Projects",
    )
    StatCard(
      modifier = Modifier.weight(1f),
      icon = "✅",
      value = stats.openTasks.toString(),
      label = "Open Tasks",
    )
  }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, icon: String, value: String, label: String) {
  Column(
    modifier =
      modifier
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
        .background(AppTheme.colorSystem.cardBackground)
        .padding(AppTheme.spacingSystem.space16)
  ) {
    Text(
      text = icon,
      style = AppTheme.typographySystem.titleMedium,
      modifier = Modifier.size(24.dp),
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    Text(
      text = value,
      style = AppTheme.typographySystem.displayMedium,
      color = AppTheme.colorSystem.textPrimary,
    )
    VerticalSpacer(AppTheme.spacingSystem.space4)
    Text(
      text = label,
      style = AppTheme.typographySystem.bodySmall,
      color = AppTheme.colorSystem.textMuted,
    )
  }
}

// ── My Tasks ──────────────────────────────────────────────────────────

@Composable
private fun MyTasksSection(tasks: List<DashboardTask>) {
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
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
        .background(AppTheme.colorSystem.cardBackground)
        .padding(AppTheme.spacingSystem.space16),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = task.title,
        style = AppTheme.typographySystem.titleSmall,
        color = AppTheme.colorSystem.textPrimary,
      )
      VerticalSpacer(AppTheme.spacingSystem.space4)
      Row {
        Text(
          text = task.productionTitle,
          style = AppTheme.typographySystem.bodySmall,
          color = AppTheme.colorSystem.textMuted,
        )
        val dueLabel = task.dueLabel
        if (dueLabel != null) {
          Text(
            text = " · ",
            style = AppTheme.typographySystem.bodySmall,
            color = AppTheme.colorSystem.textMuted,
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
      color = AppTheme.colorSystem.textMuted,
    )
  }
}

// ── Production Status ─────────────────────────────────────────────────

@Composable
private fun ProductionStatusSection(productions: List<DashboardProduction>) {
  SectionHeader(title = "Production status", actionLabel = "All projects →")
  VerticalSpacer(AppTheme.spacingSystem.space8)
  productions.forEach { production ->
    ProductionCard(production = production)
    VerticalSpacer(AppTheme.spacingSystem.space8)
  }
}

@Composable
private fun ProductionCard(production: DashboardProduction) {
  val accentColor = accentColorFor(production.accentColorHint)
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
        .background(AppTheme.colorSystem.cardBackground)
        .padding(AppTheme.spacingSystem.space16),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Color bar indicator
    Box(
      modifier =
        Modifier.width(4.dp)
          .height(40.dp)
          .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
          .background(accentColor)
    )
    Spacer(modifier = Modifier.width(AppTheme.spacingSystem.space8))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = production.title,
        style = AppTheme.typographySystem.titleSmall,
        color = AppTheme.colorSystem.textPrimary,
      )
      VerticalSpacer(AppTheme.spacingSystem.space2)
      Text(
        text = production.phase.displayLabel(),
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted,
      )
    }
    Column(horizontalAlignment = Alignment.End) {
      Text(
        text = "${production.progressPercent}%",
        style = AppTheme.typographySystem.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = accentColor,
      )
      VerticalSpacer(AppTheme.spacingSystem.space2)
      Text(
        text = "${production.daysLeft}d left",
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted,
      )
    }
  }
}

// ── Shared helpers ────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, actionLabel: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = title,
      style = AppTheme.typographySystem.titleMedium,
      color = AppTheme.colorSystem.textPrimary,
    )
    Text(
      text = actionLabel,
      style = AppTheme.typographySystem.labelMedium,
      color = AppTheme.colorSystem.accentText,
    )
  }
}

@Composable
private fun accentColorFor(hint: AccentColorHint): Color =
  when (hint) {
    AccentColorHint.GREEN -> AppTheme.colorSystem.successText
    AccentColorHint.PURPLE -> AppTheme.colorSystem.accent
    AccentColorHint.ORANGE -> AppTheme.colorSystem.warningText
  }

private fun ProductionPhase.displayLabel(): String =
  name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

// ── Preview ───────────────────────────────────────────────────────────

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
                  openTasksCount = 12,
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
                    status = TaskStatus.OPEN,
                  ),
                  DashboardTask(
                    id = "2",
                    title = "Confirm exterior shooting locations",
                    productionTitle = "Neon Wolves",
                    dueDate = LocalDate(2026, 5, 5),
                    dueLabel = "Tomorrow",
                    status = TaskStatus.OPEN,
                  ),
                  DashboardTask(
                    id = "3",
                    title = "Approve final color grade",
                    productionTitle = "The Last Frame",
                    dueDate = LocalDate(2026, 4, 28),
                    dueLabel = "Apr 28",
                    status = TaskStatus.OPEN,
                  ),
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
                    updatedAt = Instant.fromEpochSeconds(0),
                  ),
                  DashboardProduction(
                    id = "2",
                    title = "Neon Wolves",
                    phase = ProductionPhase.PRE_PRODUCTION,
                    progressPercent = 34,
                    daysLeft = 61,
                    accentColorHint = AccentColorHint.PURPLE,
                    updatedAt = Instant.fromEpochSeconds(0),
                  ),
                ),
            ),
        )
    )
  }
}
