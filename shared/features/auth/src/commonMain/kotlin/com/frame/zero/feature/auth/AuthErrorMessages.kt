package com.frame.zero.feature.auth

import com.frame.zero.domain.DomainError

internal const val EMPTY_CREDENTIALS_MESSAGE = "Email and password must not be empty"

internal fun DomainError.toUserMessage(): String =
  when (this) {
    DomainError.InvalidCredentials -> "Invalid email or password"
    DomainError.EmailAlreadyExists -> "An account with this email already exists"
    is DomainError.Network -> "Network error: $message"
    is DomainError.Unknown -> message ?: "Something went wrong"
  }
