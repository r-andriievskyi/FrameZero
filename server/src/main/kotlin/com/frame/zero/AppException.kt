package com.frame.zero

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

sealed class AppError(
  val status: HttpStatusCode,
  val code: String,
  val humanMessage: String
) {
  data object Unauthorized : AppError(HttpStatusCode.Unauthorized, "UNAUTHORIZED", "Unauthorized")

  data object Forbidden : AppError(HttpStatusCode.Forbidden, "FORBIDDEN", "Access denied")

  data object NotFound : AppError(HttpStatusCode.NotFound, "NOT_FOUND", "Resource not found")

  data class Conflict(
    val detail: String
  ) : AppError(HttpStatusCode.Conflict, "CONFLICT", detail)

  data object InvalidPhaseTransition :
    AppError(
      HttpStatusCode.Conflict,
      "INVALID_PHASE_TRANSITION",
      "Phase transition must be forward only"
    )

  data class ValidationError(
    val fields: Map<String, String>
  ) : AppError(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Validation failed")

  data class Internal(
    val detail: String
  ) : AppError(HttpStatusCode.InternalServerError, "INTERNAL", detail)
}

class AppException(
  val error: AppError
) : RuntimeException(error.humanMessage)

@Serializable
data class ErrorResponse(
  val error: String,
  val message: String,
  val fields: Map<String, String>? = null
)

fun AppError.toResponse(): ErrorResponse =
  ErrorResponse(
    error = code,
    message = humanMessage,
    fields = if (this is AppError.ValidationError) fields else null
  )
