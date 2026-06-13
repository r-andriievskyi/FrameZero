package com.frame.zero.domain

import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

/**
 * Canonical [Throwable] → [DomainError] mapping for non-auth flows. Maps HTTP
 * status codes to a distinct [DomainError] so the UI can react to 404/403/409/5xx
 * differently instead of collapsing them all into [DomainError.Unknown].
 *
 * The auth feature keeps its own mapper (401→InvalidCredentials, 409→EmailAlreadyExists)
 * because those status codes carry auth-specific meaning there.
 */
fun Throwable.toDomainError(): DomainError =
  when (this) {
    is DomainException -> error
    is IOException -> DomainError.Network(message ?: "Network error")
    is SerializationException -> DomainError.Unknown(message)
    is ResponseException ->
      when {
        response.status == HttpStatusCode.NotFound -> DomainError.NotFound
        response.status == HttpStatusCode.Unauthorized -> DomainError.Forbidden
        response.status == HttpStatusCode.Forbidden -> DomainError.Forbidden
        response.status == HttpStatusCode.Conflict -> DomainError.Conflict
        response.status.value >= 500 -> DomainError.Server(message)
        else -> DomainError.Unknown(message)
      }
    else -> DomainError.Unknown(message)
  }
