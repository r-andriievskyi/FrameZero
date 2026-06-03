package com.frame.zero.feature.auth.signin

data class SignInState(
  val email: String = "",
  val password: String = "",
  val isLoading: Boolean = false,
  val error: String? = null,
  val errorToast: String? = null
)
