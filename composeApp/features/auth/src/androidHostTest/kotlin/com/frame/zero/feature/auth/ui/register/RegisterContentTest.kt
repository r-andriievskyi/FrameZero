package com.frame.zero.feature.auth.ui.register

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frame.zero.feature.auth.register.RegisterIntent
import com.frame.zero.feature.auth.register.RegisterState
import com.frame.zero.feature.auth.ui.register.RegisterTestTags.ERROR
import com.frame.zero.feature.auth.ui.register.RegisterTestTags.SUBMIT
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.ui.UiText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Behaviour coverage for [RegisterContent]: inline error display and submit→intent wiring. */
@RunWith(RobolectricTestRunner::class)
class RegisterContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun hidesTheErrorWhenThereIsNone() {
    setContent(RegisterState())

    composeRule.onNodeWithTag(ERROR).assertDoesNotExist()
  }

  @Test
  fun showsTheInlineErrorWhenPresent() {
    setContent(RegisterState(error = UiText.Dynamic("Email already in use")))

    // The taller register form can push the inline error below the test window's fold, so assert
    // it is rendered (exists) rather than within the viewport.
    composeRule.onNodeWithTag(ERROR).assertExists()
    composeRule.onNodeWithText("Email already in use").assertExists()
  }

  @Test
  fun tappingCreateAccountEmitsSubmit() {
    val intents = mutableListOf<RegisterIntent>()
    setContent(
      state = RegisterState(firstName = "Ada", lastName = "L", email = "a@b.c", password = "secret"),
      onIntent = { intents += it }
    )

    composeRule.onNodeWithTag(SUBMIT).performClick()

    assert(intents == listOf(RegisterIntent.Submit)) { "Expected [Submit], got $intents" }
  }

  private fun setContent(
    state: RegisterState,
    onIntent: (RegisterIntent) -> Unit = {}
  ) {
    composeRule.setContent {
      AppTheme {
        RegisterContent(state = state, onIntent = onIntent, onSignInClick = {})
      }
    }
  }
}
