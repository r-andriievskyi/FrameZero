package com.frame.zero.feature.home

import com.frame.zero.ui.UiText

/**
 * A full-screen load failure. [autoRetries] is true for the offline case, where the ViewModel
 * reloads on its own once connectivity returns — the UI hides the manual retry button for that
 * case. A single nullable holder instead of two independent fields (`error`/`isOffline`) makes
 * "offline but no error message" and "error message but not tagged offline" unrepresentable.
 */
data class LoadError(
  val message: UiText,
  val autoRetries: Boolean
)
