package com.frame.zero.feature.auth

import com.frame.zero.ui.DomainErrorCategory
import com.frame.zero.ui.UiText
import com.frame.zero.ui.asUiText
import framezero.shared.features.auth.generated.resources.Res
import framezero.shared.features.auth.generated.resources.error_empty_credentials
import framezero.shared.features.auth.generated.resources.error_invalid_credentials

internal fun emptyCredentialsError(): UiText = Res.string.error_empty_credentials.asUiText()

/** Only INVALID_CREDENTIALS needs feature wording — every other category matches the shared
 *  canonical text (see DomainErrorText.kt) exactly. */
internal val authErrorMessages: Map<DomainErrorCategory, UiText> =
  mapOf(DomainErrorCategory.INVALID_CREDENTIALS to Res.string.error_invalid_credentials.asUiText())
