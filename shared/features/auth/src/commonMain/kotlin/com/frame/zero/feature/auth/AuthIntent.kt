package com.frame.zero.feature.auth

sealed interface AuthIntent {
  data class EmailChanged(val value: String) : AuthIntent

  data class PasswordChanged(val value: String) : AuthIntent

  data object LoginClicked : AuthIntent

  data object RegisterClicked : AuthIntent
}
