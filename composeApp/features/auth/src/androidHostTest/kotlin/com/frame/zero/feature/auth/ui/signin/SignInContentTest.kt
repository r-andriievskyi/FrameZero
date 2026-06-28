package com.frame.zero.feature.auth.ui.signin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frame.zero.feature.auth.signin.SignInIntent
import com.frame.zero.feature.auth.signin.SignInState
import com.frame.zero.feature.auth.ui.signin.SignInTestTags.ERROR
import com.frame.zero.feature.auth.ui.signin.SignInTestTags.SUBMIT
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.ui.UiText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Behaviour coverage for [SignInContent]: inline error display and submit→intent wiring. */
@RunWith(RobolectricTestRunner::class)
class SignInContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun hidesTheErrorWhenThereIsNone() {
    setContent(SignInState())

    composeRule.onNodeWithTag(ERROR).assertDoesNotExist()
  }

  @Test
  fun showsTheInlineErrorWhenPresent() {
    setContent(SignInState(error = UiText.Dynamic("Invalid credentials")))

    composeRule.onNodeWithTag(ERROR).assertIsDisplayed()
    composeRule.onNodeWithText("Invalid credentials").assertIsDisplayed()
  }

  @Test
  fun tappingSignInEmitsSubmit() {
    val intents = mutableListOf<SignInIntent>()
    setContent(SignInState(email = "a@b.c", password = "secret"), onIntent = { intents += it })

    composeRule.onNodeWithTag(SUBMIT).performClick()

    assert(intents == listOf(SignInIntent.Submit)) { "Expected [Submit], got $intents" }
  }

  private fun setContent(
    state: SignInState,
    onIntent: (SignInIntent) -> Unit = {}
  ) {
    composeRule.setContent {
      AppTheme {
        SignInContent(state = state, onIntent = onIntent, onCreateAccountClick = {})
      }
    }
  }
}
