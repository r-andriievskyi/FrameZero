package com.frame.zero.feature.appupdate

/**
 * What the root gate should show. Derived from the release policy vs the running build.
 *
 * - [None] — nothing to show.
 * - [Soft] — dismissable prompt; a newer build exists but the current one still works.
 * - [Hard] — blocking overlay; the current build is below the minimum supported one.
 */
sealed interface AppUpdateState {
  data object None : AppUpdateState
  data class Soft(val message: String?, val storeUrl: String) : AppUpdateState
  data class Hard(val message: String?, val storeUrl: String) : AppUpdateState
}

/** The store URL for the active prompt, or `null` when there is nothing to update to. */
val AppUpdateState.activeStoreUrl: String?
  get() = when (this) {
    is AppUpdateState.Soft -> storeUrl
    is AppUpdateState.Hard -> storeUrl
    AppUpdateState.None -> null
  }
