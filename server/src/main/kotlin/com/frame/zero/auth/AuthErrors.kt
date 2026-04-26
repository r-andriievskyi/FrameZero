package com.frame.zero.auth

import io.ktor.http.HttpStatusCode

sealed class AuthError(val status: HttpStatusCode, val message: String) {
  data object EmailAlreadyExists : AuthError(HttpStatusCode.Conflict, "Email already in use")

  data object InvalidCredentials :
    AuthError(HttpStatusCode.Unauthorized, "Invalid email or password")

  data object InvalidRefreshToken :
    AuthError(HttpStatusCode.Unauthorized, "Refresh token is invalid or expired")

  data class InvalidInput(val reason: String) : AuthError(HttpStatusCode.BadRequest, reason)
}

class AuthException(val error: AuthError) : RuntimeException(error.message)
