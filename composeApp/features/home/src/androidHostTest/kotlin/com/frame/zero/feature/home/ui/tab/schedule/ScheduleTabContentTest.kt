package com.frame.zero.feature.home.ui.tab.schedule

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frame.zero.feature.home.LoadErrorKind
import com.frame.zero.feature.home.tab.schedule.ScheduleTabState
import com.frame.zero.feature.home.ui.tab.schedule.ScheduleTabTestTags.ERROR
import com.frame.zero.feature.home.ui.tab.schedule.ScheduleTabTestTags.LOADING
import com.frame.zero.shared.design_system.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Behaviour coverage for [ScheduleTabContent]'s full-tab fallbacks (when nothing is cached): the
 * loading and offline/error states, and that the generic error's retry wires back to `onRetry`.
 * The loaded calendar/timeline is exercised by the screenshot tests.
 */
@RunWith(RobolectricTestRunner::class)
class ScheduleTabContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun showsTheProgressWhileLoadingWithNothingCached() {
    setContent(ScheduleTabState(isLoading = true, schedule = null))

    composeRule.onNodeWithTag(LOADING).assertIsDisplayed()
    composeRule.onNodeWithTag(ERROR).assertDoesNotExist()
  }

  @Test
  fun showsTheErrorWhenOfflineWithNothingCached() {
    setContent(ScheduleTabState(error = LoadErrorKind.Network, schedule = null))

    composeRule.onNodeWithTag(ERROR).assertIsDisplayed()
    composeRule.onNodeWithTag(LOADING).assertDoesNotExist()
  }

  @Test
  fun tappingRetryOnAGenericErrorEmitsRetry() {
    var retried = false
    setContent(ScheduleTabState(error = LoadErrorKind.Generic, schedule = null), onRetry = { retried = true })

    composeRule.onNodeWithText("Retry").performClick()

    assert(retried) { "Expected retry callback to fire" }
  }

  private fun setContent(
    state: ScheduleTabState,
    onRetry: () -> Unit = {}
  ) {
    composeRule.setContent {
      AppTheme {
        ScheduleTabContent(state = state, onRetry = onRetry)
      }
    }
  }
}
