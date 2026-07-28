package com.frame.zero.feature.auth

import com.frame.zero.core.error.DomainErrorMessages
import com.frame.zero.ui.UiText
import com.frame.zero.ui.asUiText
import framezero.shared.features.auth.generated.resources.Res
import framezero.shared.features.auth.generated.resources.error_email_exists
import framezero.shared.features.auth.generated.resources.error_empty_credentials
import framezero.shared.features.auth.generated.resources.error_invalid_credentials
import framezero.shared.features.auth.generated.resources.error_network
import framezero.shared.features.auth.generated.resources.error_unknown_fallback

internal fun emptyCredentialsError(): UiText = Res.string.error_empty_credentials.asUiText()

/** Sign-in/register don't surface distinct copy for NotFound/Forbidden/Conflict/Server —
 *  those collapse to the same fallback sentence a login screen would show either way. */
internal val authErrorMessages =
  DomainErrorMessages(
    network = Res.string.error_network,
    server = Res.string.error_unknown_fallback,
    notFound = Res.string.error_unknown_fallback,
    forbidden = Res.string.error_unknown_fallback,
    conflict = Res.string.error_unknown_fallback,
    invalidCredentials = Res.string.error_invalid_credentials,
    emailExists = Res.string.error_email_exists,
    fallback = Res.string.error_unknown_fallback
  )
