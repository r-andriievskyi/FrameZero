package com.frame.zero.domain

import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

fun Throwable.toDomainError(): DomainError =
  when (this) {
    is DomainException -> error
    is OfflineException -> DomainError.Offline(message ?: "No internet connection")
    is IOException -> DomainError.Server(message)
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
