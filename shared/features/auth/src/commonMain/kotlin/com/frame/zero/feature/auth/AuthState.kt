package com.frame.zero.feature.auth

enum class AuthMode {
  Login,
  Register,
}

data class AuthState(
  val mode: AuthMode = AuthMode.Login,
  val isLoading: Boolean = false,
  val error: String? = null,
)
