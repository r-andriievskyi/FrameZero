package com.frame.zero.feature.app_update

/**
 * What the root gate should show. Derived from the release policy vs the running build.
 *
 * - [None] — nothing to show.
 * - [Soft] — dismissable prompt; a newer build exists but the current one still works.
 * - [Hard] — blocking overlay; the current build is below the minimum supported one.
 */
sealed interface AppUpdateState {
  data object None : AppUpdateState

  /**
   * @param critical when `true`, the prompt surfaces regardless of network; when `false`, the
   *   controller defers it off a metered connection until the device is unmetered.
   */
  data class Soft(
    val message: String?,
    val storeUrl: String,
    val critical: Boolean
  ) : AppUpdateState

  data class Hard(
    val message: String?,
    val storeUrl: String
  ) : AppUpdateState
}

val AppUpdateState.activeStoreUrl: String?
  get() = when (this) {
    is AppUpdateState.Soft -> storeUrl
    is AppUpdateState.Hard -> storeUrl
    AppUpdateState.None -> null
  }
