package com.frame.zero.ui

/**
 * Platform-neutral user-facing text. Shared ViewModels carry a key; each platform resolves it
 * with an exhaustive `when`/`switch` (Android: `UiText.asString()` in `composeApp/shared/ui_text`),
 * so a key missing from either catalog is a compile error.
 */
sealed interface UiText {
  data class Resource(
    val key: ErrorStringKey
  ) : UiText
}

fun ErrorStringKey.asUiText(): UiText = UiText.Resource(this)
