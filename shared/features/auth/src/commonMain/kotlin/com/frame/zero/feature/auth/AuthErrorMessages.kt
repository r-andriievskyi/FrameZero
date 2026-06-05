package com.frame.zero.feature.auth

import com.frame.zero.domain.DomainError
import com.frame.zero.ui.UiText
import com.frame.zero.ui.asUiText
import framezero.shared.features.auth.generated.resources.Res
import framezero.shared.features.auth.generated.resources.error_email_exists
import framezero.shared.features.auth.generated.resources.error_empty_credentials
import framezero.shared.features.auth.generated.resources.error_invalid_credentials
import framezero.shared.features.auth.generated.resources.error_network
import framezero.shared.features.auth.generated.resources.error_unknown_fallback

internal fun emptyCredentialsError(): UiText = Res.string.error_empty_credentials.asUiText()

internal fun DomainError.toUiText(): UiText =
  when (this) {
    DomainError.InvalidCredentials -> Res.string.error_invalid_credentials.asUiText()
    DomainError.EmailAlreadyExists -> Res.string.error_email_exists.asUiText()
    is DomainError.Network -> Res.string.error_network.asUiText(message)
    is DomainError.Unknown -> message?.let(UiText::Dynamic) ?: Res.string.error_unknown_fallback.asUiText()
  }

/**
 * True for failures the user can't fix by editing the form — network outages
 * and server/unexpected errors. These surface as a transient toast rather than
 * an inline field error.
 */
internal val DomainError.isNetworkOrServerError: Boolean
  get() = this is DomainError.Network || this is DomainError.Unknown
