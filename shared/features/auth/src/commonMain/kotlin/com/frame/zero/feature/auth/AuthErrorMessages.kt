package com.frame.zero.feature.auth

import com.frame.zero.ui.DomainErrorCategory
import com.frame.zero.ui.ErrorStringKey
import com.frame.zero.ui.UiText
import com.frame.zero.ui.asUiText

internal fun emptyCredentialsError(): UiText = ErrorStringKey.AUTH_EMPTY_CREDENTIALS.asUiText()

/** Only INVALID_CREDENTIALS needs feature wording — every other category matches the shared
 *  canonical text (see DomainErrorText.kt) exactly. */
internal val authErrorMessages: Map<DomainErrorCategory, UiText> =
  mapOf(DomainErrorCategory.INVALID_CREDENTIALS to ErrorStringKey.AUTH_INVALID_CREDENTIALS.asUiText())
