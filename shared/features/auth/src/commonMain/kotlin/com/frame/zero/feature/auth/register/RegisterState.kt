package com.frame.zero.feature.auth.register

import com.frame.zero.ui.UiText

data class RegisterState(
  val firstName: String = "",
  val lastName: String = "",
  val email: String = "",
  val password: String = "",
  val isLoading: Boolean = false,
  val error: UiText? = null,
  val errorToast: UiText? = null
)
