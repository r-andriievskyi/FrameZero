package com.frame.zero.feature.auth.register

sealed interface RegisterIntent {
  data class FirstNameChanged(
    val firstName: String
  ) : RegisterIntent

  data class LastNameChanged(
    val lastName: String
  ) : RegisterIntent

  data class EmailChanged(
    val email: String
  ) : RegisterIntent

  data class PasswordChanged(
    val password: String
  ) : RegisterIntent

  data object Submit : RegisterIntent

  data object ToastDismissed : RegisterIntent
}
