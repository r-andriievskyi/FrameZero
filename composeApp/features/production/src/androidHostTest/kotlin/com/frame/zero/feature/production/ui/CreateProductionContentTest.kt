package com.frame.zero.feature.production.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.frame.zero.feature.production.CreateProductionState
import com.frame.zero.feature.production.ui.CreateProductionTestTags.step
import com.frame.zero.shared.design_system.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Behaviour coverage for the [CreateProductionContent] wizard: the `AnimatedContent` renders only
 * the step matching `state.currentStep`. Step-internal field/nav wiring is unit-tested in the
 * ViewModel; this pins the step-switching the screen owns.
 */
@RunWith(RobolectricTestRunner::class)
class CreateProductionContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun rendersTheFirstStepByDefault() {
    setContent(CreateProductionState(currentStep = 1))

    composeRule.onNodeWithTag(step(1)).assertIsDisplayed()
    composeRule.onNodeWithTag(step(2)).assertDoesNotExist()
    composeRule.onNodeWithTag(step(3)).assertDoesNotExist()
  }

  @Test
  fun rendersTheThirdStepWhenOnIt() {
    setContent(CreateProductionState(currentStep = 3))

    composeRule.onNodeWithTag(step(3)).assertIsDisplayed()
    composeRule.onNodeWithTag(step(1)).assertDoesNotExist()
    composeRule.onNodeWithTag(step(2)).assertDoesNotExist()
  }

  private fun setContent(state: CreateProductionState) {
    composeRule.setContent {
      AppTheme {
        CreateProductionContent(state = state, onIntent = {}, onBack = {})
      }
    }
  }
}
