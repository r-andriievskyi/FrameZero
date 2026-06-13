package com.frame.zero.domain

sealed interface DomainError {
  data object InvalidCredentials : DomainError

  data object EmailAlreadyExists : DomainError

  /** Resource does not (or no longer) exists — HTTP 404. */
  data object NotFound : DomainError

  /** Authenticated but not allowed — HTTP 401/403. */
  data object Forbidden : DomainError

  /** State conflict on a non-auth resource — HTTP 409. */
  data object Conflict : DomainError

  /** Server-side failure — HTTP 5xx. */
  data class Server(
    val message: String? = null
  ) : DomainError

  data class Network(
    val message: String
  ) : DomainError

  data class Unknown(
    val message: String? = null
  ) : DomainError
}

class DomainException(
  val error: DomainError
) : Exception(error.toString())
