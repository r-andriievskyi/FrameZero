package com.frame.zero.feature.auth.ui.signin.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.frame.zero.shared.design_system.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SignInPasswordFieldA11yTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun passwordToggleAnnouncesVisibilityState() {
    composeRule.setContent {
      AppTheme {
        var password by remember { mutableStateOf("secret") }
        SignInPasswordField(
          value = password,
          onValueChange = { password = it },
          enabled = true
        )
      }
    }

    val toggle = composeRule.onNodeWithContentDescription("Toggle password visibility")
    toggle.assert(
      SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Password hidden")
    )

    toggle.performClick()

    toggle.assert(
      SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Password visible")
    )
  }
}
