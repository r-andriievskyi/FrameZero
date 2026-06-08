package com.frame.zero.shared.design_system.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.frame.zero.shared.design_system.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WidgetA11yTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun ctaButtonExposesButtonRole() {
    composeRule.setContent {
      AppTheme {
        CtaButton(text = "Continue", modifier = Modifier.fillMaxWidth(), onClick = {})
      }
    }

    composeRule.onNode(hasClickAction())
      .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
  }

  @Test
  fun topToolbarBackButtonHasContentDescriptionAndRole() {
    composeRule.setContent {
      AppTheme {
        TopToolbar(title = "Summer Campaign", onBack = {})
      }
    }

    composeRule.onNodeWithContentDescription("Back")
      .assertIsDisplayed()
      .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
  }

  @Test
  fun singleLineInputFieldExposesErrorWhenSet() {
    composeRule.setContent {
      AppTheme {
        SingleLineInputField(
          value = "",
          onValueChange = {},
          errorMessage = "Email is required"
        )
      }
    }

    composeRule.onNode(hasSetTextAction())
      .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
  }

  @Test
  fun singleLineInputFieldHasNoErrorByDefault() {
    composeRule.setContent {
      AppTheme {
        SingleLineInputField(value = "", onValueChange = {})
      }
    }

    composeRule.onNode(hasSetTextAction())
      .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))
  }
}
