package com.frame.zero.feature.account.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frame.zero.feature.account.AccountState
import com.frame.zero.feature.account.ui.AccountTestTags.APP_LOCK_TOGGLE
import com.frame.zero.feature.account.ui.AccountTestTags.EDIT_PROFILE
import com.frame.zero.feature.account.ui.AccountTestTags.SIGN_OUT
import com.frame.zero.shared.design_system.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Behaviour coverage for [AccountContent]: renders the user's name/email, gates the app-lock
 * section on device support, and wires the row/toggle/sign-out actions back to their callbacks.
 */
@RunWith(RobolectricTestRunner::class)
class AccountContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun rendersUserNameAndEmail() {
    setContent(
      AccountState(userName = "Maya Chen", email = "maya@example.com", appLockSupported = true, appLockEnabled = false)
    )

    composeRule.onNodeWithText("Maya Chen").assertIsDisplayed()
    composeRule.onNodeWithText("maya@example.com").assertIsDisplayed()
  }

  @Test
  fun showsAppLockToggleWhenSupported() {
    setContent(AccountState(appLockSupported = true, appLockEnabled = false))

    composeRule.onNodeWithTag(APP_LOCK_TOGGLE).assertIsDisplayed()
  }

  @Test
  fun hidesAppLockToggleWhenUnsupported() {
    setContent(AccountState(appLockSupported = false, appLockEnabled = false))

    composeRule.onNodeWithTag(APP_LOCK_TOGGLE).assertDoesNotExist()
  }

  @Test
  fun tappingEditProfileInvokesItsCallback() {
    var clicked = false
    setContent(
      state = AccountState(userName = "Maya", appLockSupported = false, appLockEnabled = false),
      onEditProfileClick = { clicked = true }
    )

    composeRule.onNodeWithTag(EDIT_PROFILE).performClick()

    assert(clicked) { "Expected edit-profile callback to fire" }
  }

  @Test
  fun tappingSignOutInvokesItsCallback() {
    var signedOut = false
    setContent(
      state = AccountState(appLockSupported = false, appLockEnabled = false),
      onSignOutClick = { signedOut = true }
    )

    composeRule.onNodeWithTag(SIGN_OUT).performClick()

    assert(signedOut) { "Expected sign-out callback to fire" }
  }

  @Test
  fun togglingAppLockReportsTheNewValue() {
    val toggles = mutableListOf<Boolean>()
    setContent(
      state = AccountState(appLockSupported = true, appLockEnabled = false),
      onAppLockToggle = { enabled, _ -> toggles += enabled }
    )

    // The row is inert except the switch, so drive the toggleable node directly.
    composeRule.onNode(isToggleable()).performClick()

    assert(toggles == listOf(true)) { "Expected [true] from an off→on toggle, got $toggles" }
  }

  private fun setContent(
    state: AccountState,
    onEditProfileClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onAppLockToggle: (Boolean, com.frame.zero.core.security.BiometricPromptText) -> Unit = { _, _ -> }
  ) {
    composeRule.setContent {
      AppTheme {
        AccountContent(
          state = state,
          onBack = {},
          onEditProfileClick = onEditProfileClick,
          onEmailClick = {},
          onPasswordSecurityClick = {},
          onNotificationsClick = {},
          onAppLockToggle = onAppLockToggle,
          onSignOutClick = onSignOutClick
        )
      }
    }
  }
}
