package com.frame.zero.feature.auth

data class AuthState(
  val email: String = "",
  val password: String = "",
  val isLoading: Boolean = false,
  val error: String? = null,
)
