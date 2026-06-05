package com.frame.zero.feature.home.ui.tab.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.frame.zero.feature.home.tab.dashboard.DashboardStatsUi
import com.frame.zero.feature.home.tab.dashboard.DashboardTaskUi
import com.frame.zero.feature.home.tab.dashboard.DashboardUi
import com.frame.zero.feature.home.tab.dashboard.DueUrgency
import com.frame.zero.feature.home.ui.tab.dashboard.DashboardTestTags.GREETING
import com.frame.zero.feature.home.ui.tab.dashboard.DashboardTestTags.MY_TASKS_SECTION
import com.frame.zero.feature.home.ui.tab.dashboard.DashboardTestTags.STAT_ACTIVE_PRODUCTIONS
import com.frame.zero.feature.home.ui.tab.dashboard.DashboardTestTags.STAT_OPEN_TASKS
import com.frame.zero.feature.home.ui.tab.dashboard.DashboardTestTags.taskRow
import com.frame.zero.shared.design_system.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DashboardTabContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun rendersUsersTask() {
    composeRule.setContent {
      AppTheme {
        DashboardContent(
          dashboard = dashboard(
            myTasks = listOf(
              task(
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

    composeRule.onNodeWithTag(taskRow("1"))
      .assertTextContains("Review Scene 12 script revisions")
      .assertTextContains("Echoes of Silence")
  }

  @Test
  fun rendersGreetingWithDisplayName() {
    composeRule.setContent {
      AppTheme {
        DashboardContent(dashboard = dashboard(displayName = "Maya"), onTaskClick = {})
      }
    }

    composeRule.onNodeWithTag(GREETING).assertTextContains("Maya", substring = true)
  }

  @Test
  fun rendersStatsValuesAndLabels() {
    composeRule.setContent {
      AppTheme {
        DashboardContent(
          dashboard = dashboard(stats = DashboardStatsUi(activeProjects = 3, openTasks = 12)),
          onTaskClick = {}
        )
      }
    }

    composeRule.onNodeWithTag(STAT_ACTIVE_PRODUCTIONS).assertTextContains("3")
    composeRule.onNodeWithTag(STAT_OPEN_TASKS).assertTextContains("12")
  }

  @Test
  fun rendersMyTasksHeaderWhenTasksPresent() {
    composeRule.setContent {
      AppTheme {
        DashboardContent(
          dashboard = dashboard(myTasks = listOf(task(id = "1"))),
          onTaskClick = {}
        )
      }
    }

    composeRule.onNodeWithTag(MY_TASKS_SECTION).assertIsDisplayed()
  }

  @Test
  fun hidesMyTasksSectionWhenTasksEmpty() {
    composeRule.setContent {
      AppTheme {
        DashboardContent(dashboard = dashboard(myTasks = emptyList()), onTaskClick = {})
      }
    }

    composeRule.onNodeWithTag(MY_TASKS_SECTION).assertDoesNotExist()
  }

  @Test
  fun rendersNothingWhenDashboardIsNull() {
    composeRule.setContent {
      AppTheme {
        DashboardContent(dashboard = null, onTaskClick = {})
      }
    }

    composeRule.onNodeWithTag(GREETING).assertDoesNotExist()
    composeRule.onNodeWithTag(MY_TASKS_SECTION).assertDoesNotExist()
  }

  @Test
  fun rendersAllTasksInList() {
    composeRule.setContent {
      AppTheme {
        DashboardContent(
          dashboard = dashboard(
            myTasks = listOf(
              task(id = "1", title = "Review Scene 12 script revisions", productionTitle = "Echoes of Silence"),
              task(id = "2", title = "Confirm exterior shooting locations", productionTitle = "Neon Wolves"),
              task(id = "3", title = "Approve final color grade", productionTitle = "The Last Frame")
            )
          ),
          onTaskClick = {}
        )
      }
    }

    composeRule.onNodeWithTag(taskRow("1"))
      .assertTextContains("Review Scene 12 script revisions")
      .assertTextContains("Echoes of Silence")
    composeRule.onNodeWithTag(taskRow("2"))
      .assertTextContains("Confirm exterior shooting locations")
      .assertTextContains("Neon Wolves")
    composeRule.onNodeWithTag(taskRow("3"))
      .assertTextContains("Approve final color grade")
      .assertTextContains("The Last Frame")
  }

  @Test
  fun rendersDueLabelWhenProvided() {
    composeRule.setContent {
      AppTheme {
        DashboardContent(
          dashboard = dashboard(myTasks = listOf(task(id = "1", dueLabel = "Apr 28"))),
          onTaskClick = {}
        )
      }
    }

    composeRule.onNodeWithTag(taskRow("1")).assertTextContains("Apr 28")
  }

  @Test
  fun rendersTaskWithoutDueLabel() {
    composeRule.setContent {
      AppTheme {
        DashboardContent(
          dashboard = dashboard(
            myTasks = listOf(
              task(id = "1", title = "Task without due date", productionTitle = "Project X", dueLabel = null)
            )
          ),
          onTaskClick = {}
        )
      }
    }

    composeRule.onNodeWithTag(taskRow("1"))
      .assertTextContains("Task without due date")
      .assertTextContains("Project X")
  }

  @Test
  fun invokesOnTaskClickWithTaskId() {
    val clicked = mutableListOf<String>()
    composeRule.setContent {
      AppTheme {
        DashboardContent(
          dashboard = dashboard(
            myTasks = listOf(
              task(id = "task-1", title = "First task"),
              task(id = "task-2", title = "Second task")
            )
          ),
          onTaskClick = { clicked += it }
        )
      }
    }

    composeRule.onNodeWithTag(taskRow("task-2")).performClick()

    assert(clicked == listOf("task-2")) { "Expected [task-2], got $clicked" }
  }

  private fun dashboard(
    displayName: String = "Maya",
    stats: DashboardStatsUi = DashboardStatsUi(activeProjects = 3, openTasks = 12),
    myTasks: List<DashboardTaskUi> = listOf(task())
  ): DashboardUi = DashboardUi(displayName = displayName, stats = stats, myTasks = myTasks)

  private fun task(
    id: String = "1",
    title: String = "Review Scene 12 script revisions",
    productionTitle: String = "Echoes of Silence",
    dueLabel: String? = "Today",
    dueUrgency: DueUrgency = DueUrgency.Today
  ): DashboardTaskUi =
    DashboardTaskUi(
      id = id,
      title = title,
      productionTitle = productionTitle,
      dueLabel = dueLabel,
      dueUrgency = dueUrgency
    )
}
