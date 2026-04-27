package com.frame.zero.feature.auth

sealed interface AuthIntent {
  data class Login(val email: String, val password: String) : AuthIntent

  data class Register(val email: String, val password: String) : AuthIntent

  data object SwitchMode : AuthIntent
}
