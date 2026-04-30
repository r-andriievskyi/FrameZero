package com.frame.zero.feature.auth.register

sealed interface RegisterIntent {
  data class NameChanged(val name: String) : RegisterIntent

  data class EmailChanged(val email: String) : RegisterIntent

  data class PasswordChanged(val password: String) : RegisterIntent

  data object Submit : RegisterIntent
}
