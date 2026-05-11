package com.frame.zero.common

import com.frame.zero.AppError
import com.frame.zero.AppException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import java.time.ZoneId
import java.util.UUID

fun ApplicationCall.userId(): UUID {
  val subject = principal<JWTPrincipal>()?.subject ?: throw AppException(AppError.Unauthorized)
  return runCatching { UUID.fromString(subject) }
    .getOrElse { throw AppException(AppError.Unauthorized) }
}

fun ApplicationCall.pathUuid(name: String): UUID =
  runCatching { UUID.fromString(parameters[name]) }
    .getOrElse { throw AppException(AppError.NotFound) }

fun ApplicationCall.timezone(): ZoneId =
  request.headers["X-Timezone"]?.let {
    runCatching { ZoneId.of(it) }.getOrDefault(ZoneId.of("UTC"))
  } ?: ZoneId.of("UTC")
