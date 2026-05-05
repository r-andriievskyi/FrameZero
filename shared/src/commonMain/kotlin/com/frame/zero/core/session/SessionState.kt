package com.frame.zero.core.session

import com.frame.zero.domain.User

sealed interface SessionState {
  data object Loading : SessionState

  data object LoggedOut : SessionState

  data class LoggedIn(
    val user: User
  ) : SessionState
}
