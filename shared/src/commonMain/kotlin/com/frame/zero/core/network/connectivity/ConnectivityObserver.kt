package com.frame.zero.core.network.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * Observes the device's network reachability. Implementations emit the current
 * state immediately on collection and then a new value on every change.
 *
 * Platform-specific (Android [android.net.ConnectivityManager], iOS `NWPathMonitor`);
 * the concrete impls live in the platform source sets and are wired through Koin's
 * `platformModule()`. Mirrors the `DatabaseBuilderFactory` blueprint.
 */
interface ConnectivityObserver {
  /** `true` while the device has a validated internet connection. Conflated and
   *  distinct — only real transitions are emitted. */
  val isOnline: Flow<Boolean>

  /** Synchronous snapshot of the current reachability. Used to fail requests fast
   *  when the device is offline instead of waiting for a connect timeout. */
  fun isCurrentlyOnline(): Boolean
}
