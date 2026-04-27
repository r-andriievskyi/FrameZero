package com.frame.zero.feature.auth.data

import com.frame.zero.domain.DomainError
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

internal fun Throwable.toDomainError(): DomainError =
  when (this) {
    is ResponseException ->
      when (response.status) {
        HttpStatusCode.Unauthorized -> DomainError.InvalidCredentials
        HttpStatusCode.Conflict -> DomainError.EmailAlreadyExists
        else -> DomainError.Unknown(message)
      }
    is IOException -> DomainError.Network(message ?: "Network error")
    is SerializationException -> DomainError.Unknown(message)
    else -> DomainError.Unknown(message)
  }
