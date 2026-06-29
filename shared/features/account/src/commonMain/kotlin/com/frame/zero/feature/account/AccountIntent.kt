package com.frame.zero.feature.account

import com.frame.zero.core.security.BiometricPromptText

sealed interface AccountIntent {
  data class AppLockToggled(
    val enabled: Boolean,
    val prompt: BiometricPromptText
  ) : AccountIntent

  data object SignOutClicked : AccountIntent
}
