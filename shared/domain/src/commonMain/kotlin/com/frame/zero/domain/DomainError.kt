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

  data object Server : DomainError

  data object Offline : DomainError

  data object InsufficientStorage : DomainError

  /** Attachment (or other payload) exceeds the server's size limit — HTTP 413. */
  data object PayloadTooLarge : DomainError

  /** Production phase transitions must be forward-only — HTTP 409. */
  data object InvalidPhaseTransition : DomainError

  /** Per-field validation failures from the server — HTTP 400. */
  data class Validation(
    val fields: Map<String, String>
  ) : DomainError

  data object Unknown : DomainError
}

class DomainException(
  val error: DomainError
) : Exception(error.toString())
