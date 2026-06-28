package com.frame.zero.feature.task.details.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frame.zero.feature.task.details.TaskDetailsIntent
import com.frame.zero.feature.task.details.TaskDetailsState
import com.frame.zero.feature.task.details.ui.TaskDetailsTestTags.CONTENT
import com.frame.zero.feature.task.details.ui.TaskDetailsTestTags.ERROR
import com.frame.zero.feature.task.details.ui.TaskDetailsTestTags.LOADING
import com.frame.zero.feature.task.details.ui.TaskDetailsTestTags.MARK_COMPLETE
import com.frame.zero.shared.design_system.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Behaviour coverage for [TaskDetailsContent]: the loading / error / content states the `when`
 * selects, and that the mark-complete CTA wires to [TaskDetailsIntent.MarkComplete].
 */
@RunWith(RobolectricTestRunner::class)
class TaskDetailsContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun showsOnlyTheProgressWhileLoading() {
    setContent(TaskDetailsState(isLoading = true))

    composeRule.onNodeWithTag(LOADING).assertIsDisplayed()
    composeRule.onNodeWithTag(ERROR).assertDoesNotExist()
    composeRule.onNodeWithTag(CONTENT).assertDoesNotExist()
  }

  @Test
  fun showsOnlyTheErrorWhenLoadFails() {
    setContent(TaskDetailsState(isError = true))

    composeRule.onNodeWithTag(ERROR).assertIsDisplayed()
    composeRule.onNodeWithTag(LOADING).assertDoesNotExist()
    composeRule.onNodeWithTag(CONTENT).assertDoesNotExist()
  }

  @Test
  fun showsTheContentWithTitleWhenLoaded() {
    setContent(TaskDetailsState(title = "Lock the schedule", productionName = "Echoes of Silence"))

    composeRule.onNodeWithTag(CONTENT).assertIsDisplayed()
    composeRule.onNodeWithText("Lock the schedule").assertIsDisplayed()
    composeRule.onNodeWithText("Echoes of Silence").assertIsDisplayed()
    composeRule.onNodeWithTag(LOADING).assertDoesNotExist()
    composeRule.onNodeWithTag(ERROR).assertDoesNotExist()
  }

  @Test
  fun hidesMarkCompleteButtonUntilItIsAvailable() {
    setContent(TaskDetailsState(title = "T", showMarkCompleteButton = false))

    composeRule.onNodeWithTag(MARK_COMPLETE).assertDoesNotExist()
  }

  @Test
  fun tappingMarkCompleteEmitsTheIntent() {
    val intents = mutableListOf<TaskDetailsIntent>()
    setContent(
      state = TaskDetailsState(title = "T", showMarkCompleteButton = true),
      onIntent = { intents += it }
    )

    composeRule.onNodeWithTag(MARK_COMPLETE).performClick()

    assert(intents == listOf(TaskDetailsIntent.MarkComplete)) { "Expected [MarkComplete], got $intents" }
  }

  private fun setContent(
    state: TaskDetailsState,
    onIntent: (TaskDetailsIntent) -> Unit = {}
  ) {
    composeRule.setContent {
      AppTheme {
        TaskDetailsContent(state = state, onBack = {}, onIntent = onIntent)
      }
    }
  }
}
