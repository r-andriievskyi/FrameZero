package com.frame.zero.domain

/**
 * Raised by the network layer once it has parsed a server error body — carries the
 * structured wire fields ([code]/[fields]) rather than the server's free-text message.
 *
 * The exception message is deliberately just `code (status)`: enough to identify the failure
 * in a log line or Crashlytics non-fatal, while excluding the server's human-readable
 * `message` and the [fields] values, either of which may carry PII. Nothing here reaches the
 * UI regardless — [DomainError] carries no message, so [toDomainError] drops all of it.
 */
class ServerErrorException(
  val code: String,
  val status: Int,
  val fields: Map<String, String>? = null
) : Exception("$code ($status)")
