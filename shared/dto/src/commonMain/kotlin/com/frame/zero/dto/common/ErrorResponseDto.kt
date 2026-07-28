package com.frame.zero.dto.common

import kotlinx.serialization.Serializable

/** Client copy of server `AppException.kt`'s `ErrorResponse` wire shape — edit both in sync. */
@Serializable
data class ErrorResponseDto(
  val error: String,
  val message: String,
  val fields: Map<String, String>? = null
)
