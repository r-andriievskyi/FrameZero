package com.frame.zero.feature.force_update

/**
 * What the root gate should show. Derived from the release policy vs the running build.
 *
 * - [None] — nothing to show.
 * - [Soft] — dismissable prompt; a newer build exists but the current one still works.
 * - [Hard] — blocking overlay; the current build is below the minimum supported one.
 */
sealed interface ForceUpdateState {
  data object None : ForceUpdateState

  /**
   * @param critical when `true`, the prompt surfaces regardless of network; when `false`, the
   *   controller defers it off a metered connection until the device is unmetered.
   */
  data class Soft(
    val message: String?,
    val storeUrl: String,
    val critical: Boolean
  ) : ForceUpdateState

  data class Hard(
    val message: String?,
    val storeUrl: String
  ) : ForceUpdateState
}

val ForceUpdateState.activeStoreUrl: String?
  get() = when (this) {
    is ForceUpdateState.Soft -> storeUrl
    is ForceUpdateState.Hard -> storeUrl
    ForceUpdateState.None -> null
  }
