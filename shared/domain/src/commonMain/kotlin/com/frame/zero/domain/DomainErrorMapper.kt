package com.frame.zero.domain

import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

fun Throwable.toDomainError(): DomainError =
  when (this) {
    is DomainException -> error
    is OfflineException -> DomainError.Offline
    is ServerErrorException -> toDomainErrorByCode()
    is ResponseException -> response.status.toDomainErrorByStatus()
    is IOException -> DomainError.Server
    is SerializationException -> DomainError.Unknown
    else -> DomainError.Unknown
  }

/** Maps the server's `error` code first — see `AppError` (server) for the 11-code wire vocabulary. */
private fun ServerErrorException.toDomainErrorByCode(): DomainError =
  when (code) {
    "INVALID_CREDENTIALS" -> DomainError.InvalidCredentials
    "EMAIL_ALREADY_EXISTS" -> DomainError.EmailAlreadyExists
    "NOT_FOUND" -> DomainError.NotFound
    "UNAUTHORIZED", "INVALID_REFRESH_TOKEN", "FORBIDDEN" -> DomainError.Forbidden
    "CONFLICT" -> DomainError.Conflict
    "PAYLOAD_TOO_LARGE" -> DomainError.PayloadTooLarge
    "INVALID_PHASE_TRANSITION" -> DomainError.InvalidPhaseTransition
    "VALIDATION_ERROR" -> DomainError.Validation(fields.orEmpty())
    "INTERNAL" -> DomainError.Server
    else -> HttpStatusCode.fromValue(status).toDomainErrorByStatus()
  }

private fun HttpStatusCode.toDomainErrorByStatus(): DomainError =
  when {
    this == HttpStatusCode.NotFound -> DomainError.NotFound
    this == HttpStatusCode.Unauthorized -> DomainError.Forbidden
    this == HttpStatusCode.Forbidden -> DomainError.Forbidden
    this == HttpStatusCode.Conflict -> DomainError.Conflict
    value >= 500 -> DomainError.Server
    else -> DomainError.Unknown
  }
