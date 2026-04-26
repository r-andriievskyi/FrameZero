package com.frame.zero.feature.auth

sealed interface AuthEvent {
  data object Authenticated : AuthEvent
}
