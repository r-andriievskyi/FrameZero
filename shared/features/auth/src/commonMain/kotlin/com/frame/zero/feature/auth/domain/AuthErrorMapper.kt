package com.frame.zero.feature.auth.domain

import com.frame.zero.domain.OfflineException
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.DomainException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

internal fun Throwable.toDomainError(): DomainError =
  when (this) {
    is DomainException -> error
    is ResponseException ->
      when (response.status) {
        HttpStatusCode.Unauthorized -> DomainError.InvalidCredentials
        HttpStatusCode.Conflict -> DomainError.EmailAlreadyExists
        else -> DomainError.Unknown(message)
      }
    is OfflineException -> DomainError.Offline(message ?: "No internet connection")
    is IOException -> DomainError.Server(message)
    is SerializationException -> DomainError.Unknown(message)
    else -> DomainError.Unknown(message)
  }
