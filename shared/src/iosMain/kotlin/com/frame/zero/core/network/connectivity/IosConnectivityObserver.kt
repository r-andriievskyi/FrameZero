package com.frame.zero.core.network.connectivity

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_is_expensive
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_queue_create

/**
 * [ConnectivityObserver] backed by `NWPathMonitor`. A single monitor runs for the
 * app's lifetime (this is `@SingleIn(AppScope::class)`) and feeds a [MutableStateFlow]
 * so both the reactive [isOnline] stream and the synchronous [isCurrentlyOnline]
 * snapshot read the same state.
 */
@OptIn(ExperimentalForeignApi::class)
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class IosConnectivityObserver : ConnectivityObserver {
  private val state = MutableStateFlow(true)

  // Unmetered until the monitor reports otherwise, so a non-critical soft update is never
  // wrongly deferred before the first path update.
  private val metered = MutableStateFlow(false)

  init {
    val monitor = nw_path_monitor_create()
    val queue = dispatch_queue_create("com.frame.zero.connectivity", null)
    nw_path_monitor_set_update_handler(monitor) { path ->
      state.value = nw_path_get_status(path) == nw_path_status_satisfied
      metered.value = nw_path_is_expensive(path)
    }
    nw_path_monitor_set_queue(monitor, queue)
    nw_path_monitor_start(monitor)
  }

  override val isOnline: Flow<Boolean> = state.asStateFlow()

  override fun isCurrentlyOnline(): Boolean = state.value

  override val isMetered: Flow<Boolean> = metered.asStateFlow()

  override fun isCurrentlyMetered(): Boolean = metered.value
}
