package com.frame.zero.routes

import com.frame.zero.AppError
import com.frame.zero.AppException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import java.util.UUID

fun ApplicationCall.userId(): UUID {
  val subject =
    principal<JWTPrincipal>()?.subject ?: throw AppException(AppError.Unauthorized)
  return runCatching { UUID.fromString(subject) }.getOrElse { throw AppException(AppError.Unauthorized) }
}

fun ApplicationCall.pathUuid(name: String): UUID =
  runCatching { UUID.fromString(parameters[name]) }.getOrElse {
    throw AppException(AppError.NotFound)
  }
