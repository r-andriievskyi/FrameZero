package com.frame.zero.feature.account

data class AccountState(
  val userName: String? = null,
  val email: String? = null,
  val appLockSupported: Boolean,
  val appLockEnabled: Boolean
)
