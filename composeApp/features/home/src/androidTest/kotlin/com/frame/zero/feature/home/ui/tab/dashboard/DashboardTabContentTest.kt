package com.frame.zero.feature.home.ui.tab.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frame.zero.feature.home.tab.dashboard.DashboardStatsUi
import com.frame.zero.feature.home.tab.dashboard.DashboardTaskUi
import com.frame.zero.feature.home.tab.dashboard.DashboardUi
import com.frame.zero.shared.design_system.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardTabContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun rendersUsersTaskTitle() {
    composeRule.setContent {
      AppTheme {
        DashboardContent(
          dashboard = DashboardUi(
            displayName = "Maya",
            stats = DashboardStatsUi(activeProjects = 3, openTasks = 12),
            myTasks = listOf(
              DashboardTaskUi(
                id = "1",
                title = "Review Scene 12 script revisions",
                productionTitle = "Echoes of Silence",
                dueLabel = "Today"
              )
            )
          ),
          onTaskClick = {}
        )
      }
    }

    composeRule.onNodeWithText("Review Scene 12 script revisions").assertIsDisplayed()
  }
}
