package com.frame.zero.feature.task.create.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frame.zero.feature.task.create.CreateTaskIntent
import com.frame.zero.feature.task.create.CreateTaskState
import com.frame.zero.feature.task.create.ui.CreateTaskTestTags.SUBMIT
import com.frame.zero.feature.task.create.ui.CreateTaskTestTags.TITLE_ERROR
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.generated.resources.Res
import com.frame.zero.shared.design_system.generated.resources.error_generic_message
import com.frame.zero.ui.asUiText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Behaviour coverage for [CreateTaskContent]: title validation error display and submit→intent. */
@RunWith(RobolectricTestRunner::class)
class CreateTaskContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun hidesTitleErrorWhenThereIsNone() {
    setContent(CreateTaskState())

    composeRule.onNodeWithTag(TITLE_ERROR).assertDoesNotExist()
  }

  @Test
  fun showsTitleErrorWhenPresent() {
    setContent(CreateTaskState(titleError = Res.string.error_generic_message.asUiText()))

    composeRule.onNodeWithTag(TITLE_ERROR).assertIsDisplayed()
    composeRule.onNodeWithText("Something went wrong").assertIsDisplayed()
  }

  @Test
  fun tappingCreateEmitsSubmit() {
    val intents = mutableListOf<CreateTaskIntent>()
    setContent(CreateTaskState(title = "Lock the schedule"), onIntent = { intents += it })

    composeRule.onNodeWithTag(SUBMIT).performClick()

    assert(intents == listOf(CreateTaskIntent.Submit)) { "Expected [Submit], got $intents" }
  }

  private fun setContent(
    state: CreateTaskState,
    onIntent: (CreateTaskIntent) -> Unit = {}
  ) {
    composeRule.setContent {
      AppTheme {
        CreateTaskContent(state = state, onIntent = onIntent, onBack = {})
      }
    }
  }
}
