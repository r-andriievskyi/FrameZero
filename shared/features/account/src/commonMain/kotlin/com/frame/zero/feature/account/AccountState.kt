package com.frame.zero.feature.account

import com.frame.zero.ui.UiText

data class AccountState(
  val userName: String? = null,
  val email: String? = null,
  val appLockSupported: Boolean,
  val appLockEnabled: Boolean,
  val developerOptionsEnabled: Boolean = false,
  val appLockError: UiText? = null
)
