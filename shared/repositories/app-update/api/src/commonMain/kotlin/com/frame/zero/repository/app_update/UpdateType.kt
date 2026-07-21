package com.frame.zero.repository.app_update

/**
 * How urgently the installed client must update, derived by comparing the running build against
 * the [UpdatePolicy] thresholds.
 *
 * - [NONE] — current build is at or above the latest release; nothing to prompt.
 * - [SOFT] — a newer build exists but the current one still works; prompt is dismissable.
 * - [HARD] — current build is below the minimum supported one; the app is gated until updated.
 */
enum class UpdateType { NONE, SOFT, HARD }
