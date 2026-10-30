package com.frame.zero.ui

import com.frame.zero.domain.DomainError

/**
 * The category a [DomainError] falls into for display purposes — coarser than the domain type
 * itself (e.g. [DomainError.Conflict] and [DomainError.InvalidPhaseTransition] both read as
 * [CONFLICT]), and independent of any one feature's wording. A feature overrides only the
 * categories where it genuinely needs different copy — most only need FORBIDDEN/CONFLICT/NOT_FOUND,
 * since those are the ones that read better naming the entity involved ("this production", "this
 * task"); network/server/fallback are copy-pasted identically almost everywhere and so default to
 * shared text.
 */
enum class DomainErrorCategory {
  NETWORK,
  SERVER,
  NOT_FOUND,
  FORBIDDEN,
  CONFLICT,
  INVALID_CREDENTIALS,
  EMAIL_EXISTS,
  INSUFFICIENT_STORAGE,
  PAYLOAD_TOO_LARGE,
  VALIDATION,
  FALLBACK
}

fun DomainError.category(): DomainErrorCategory =
  when (this) {
    DomainError.Offline -> DomainErrorCategory.NETWORK
    DomainError.Server -> DomainErrorCategory.SERVER
    DomainError.NotFound -> DomainErrorCategory.NOT_FOUND
    DomainError.Forbidden -> DomainErrorCategory.FORBIDDEN
    DomainError.Conflict,
    DomainError.InvalidPhaseTransition -> DomainErrorCategory.CONFLICT
    DomainError.InvalidCredentials -> DomainErrorCategory.INVALID_CREDENTIALS
    DomainError.EmailAlreadyExists -> DomainErrorCategory.EMAIL_EXISTS
    DomainError.InsufficientStorage -> DomainErrorCategory.INSUFFICIENT_STORAGE
    DomainError.PayloadTooLarge -> DomainErrorCategory.PAYLOAD_TOO_LARGE
    is DomainError.Validation -> DomainErrorCategory.VALIDATION
    DomainError.Unknown -> DomainErrorCategory.FALLBACK
  }

private fun DomainErrorCategory.canonicalUiText(): UiText =
  when (this) {
    DomainErrorCategory.NETWORK -> ErrorStringKey.NETWORK
    DomainErrorCategory.SERVER -> ErrorStringKey.SERVER
    DomainErrorCategory.NOT_FOUND -> ErrorStringKey.NOT_FOUND
    DomainErrorCategory.FORBIDDEN -> ErrorStringKey.FORBIDDEN
    DomainErrorCategory.CONFLICT -> ErrorStringKey.CONFLICT
    DomainErrorCategory.INVALID_CREDENTIALS -> ErrorStringKey.INVALID_CREDENTIALS
    DomainErrorCategory.EMAIL_EXISTS -> ErrorStringKey.EMAIL_EXISTS
    DomainErrorCategory.INSUFFICIENT_STORAGE -> ErrorStringKey.INSUFFICIENT_STORAGE
    DomainErrorCategory.PAYLOAD_TOO_LARGE,
    DomainErrorCategory.VALIDATION,
    DomainErrorCategory.FALLBACK -> ErrorStringKey.UNKNOWN
  }.asUiText()

/**
 * Maps a [DomainError] to user-facing [UiText]. [overrides] lets a feature swap in its own
 * copy for specific categories (built from that feature's own [ErrorStringKey]s); anything not
 * overridden falls back to the shared canonical text for its [category].
 */
fun DomainError.toUiText(overrides: Map<DomainErrorCategory, UiText> = emptyMap()): UiText {
  val category = category()
  return overrides[category] ?: category.canonicalUiText()
}

val DomainError.isOfflineOrServerError: Boolean
  get() = this is DomainError.Offline || this is DomainError.Server || this is DomainError.Unknown
