package com.frame.zero.feature.auth.signin

sealed interface SignInIntent {
  data class EmailChanged(
    val email: String
  ) : SignInIntent

  data class PasswordChanged(
    val password: String
  ) : SignInIntent

  data object Submit : SignInIntent

  data object ToastDismissed : SignInIntent
}
