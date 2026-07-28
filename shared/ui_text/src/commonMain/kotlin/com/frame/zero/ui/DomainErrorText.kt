package com.frame.zero.ui

import com.frame.zero.domain.DomainError
import framezero.shared.ui_text.generated.resources.Res
import framezero.shared.ui_text.generated.resources.error_conflict
import framezero.shared.ui_text.generated.resources.error_email_exists
import framezero.shared.ui_text.generated.resources.error_forbidden
import framezero.shared.ui_text.generated.resources.error_insufficient_storage
import framezero.shared.ui_text.generated.resources.error_invalid_credentials
import framezero.shared.ui_text.generated.resources.error_network
import framezero.shared.ui_text.generated.resources.error_not_found
import framezero.shared.ui_text.generated.resources.error_server
import framezero.shared.ui_text.generated.resources.error_unknown_fallback

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
    DomainError.PayloadTooLarge,
    is DomainError.Validation,
    DomainError.Unknown -> DomainErrorCategory.FALLBACK
  }

private fun DomainErrorCategory.canonicalUiText(): UiText =
  when (this) {
    DomainErrorCategory.NETWORK -> Res.string.error_network
    DomainErrorCategory.SERVER -> Res.string.error_server
    DomainErrorCategory.NOT_FOUND -> Res.string.error_not_found
    DomainErrorCategory.FORBIDDEN -> Res.string.error_forbidden
    DomainErrorCategory.CONFLICT -> Res.string.error_conflict
    DomainErrorCategory.INVALID_CREDENTIALS -> Res.string.error_invalid_credentials
    DomainErrorCategory.EMAIL_EXISTS -> Res.string.error_email_exists
    DomainErrorCategory.INSUFFICIENT_STORAGE -> Res.string.error_insufficient_storage
    DomainErrorCategory.FALLBACK -> Res.string.error_unknown_fallback
  }.asUiText()

/**
 * Maps a [DomainError] to user-facing [UiText]. [overrides] lets a feature swap in its own
 * copy for specific categories (built from that feature's own string resources); anything not
 * overridden falls back to the shared canonical text for its [category].
 */
fun DomainError.toUiText(overrides: Map<DomainErrorCategory, UiText> = emptyMap()): UiText {
  val category = category()
  return overrides[category] ?: category.canonicalUiText()
}

val DomainError.isOfflineOrServerError: Boolean
  get() = this is DomainError.Offline || this is DomainError.Server || this is DomainError.Unknown
