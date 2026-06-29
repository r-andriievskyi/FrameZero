package com.frame.zero.core.error

import com.frame.zero.domain.DomainError
import com.frame.zero.ui.UiText
import com.frame.zero.ui.asUiText
import org.jetbrains.compose.resources.StringResource

data class DomainErrorMessages(
  val network: StringResource,
  val server: StringResource,
  val notFound: StringResource,
  val forbidden: StringResource,
  val conflict: StringResource,
  val invalidCredentials: StringResource,
  val emailExists: StringResource,
  /** Catch-all for [DomainError.InsufficientStorage], [DomainError.Unknown], and any
   *  category the feature doesn't surface a dedicated string for. */
  val fallback: StringResource
)

/** Maps a [DomainError] to a user-facing [UiText] using a feature's [messages]. */
fun DomainError.toUiText(messages: DomainErrorMessages): UiText =
  when (this) {
    is DomainError.Offline -> messages.network.asUiText()
    is DomainError.Server -> messages.server.asUiText()
    DomainError.NotFound -> messages.notFound.asUiText()
    DomainError.Forbidden -> messages.forbidden.asUiText()
    DomainError.Conflict -> messages.conflict.asUiText()
    DomainError.InvalidCredentials -> messages.invalidCredentials.asUiText()
    DomainError.EmailAlreadyExists -> messages.emailExists.asUiText()
    DomainError.InsufficientStorage,
    is DomainError.Unknown -> messages.fallback.asUiText()
  }

val DomainError.isOfflineOrServerError: Boolean
  get() = this is DomainError.Offline || this is DomainError.Server || this is DomainError.Unknown
