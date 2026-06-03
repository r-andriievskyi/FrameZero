package com.frame.zero.feature.auth.register

data class RegisterState(
  val firstName: String = "",
  val lastName: String = "",
  val email: String = "",
  val password: String = "",
  val isLoading: Boolean = false,
  val error: String? = null,
  val errorToast: String? = null
)
