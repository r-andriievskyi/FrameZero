package com.frame.zero.core.network.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.callbackFlow

/**
 * [ConnectivityObserver] backed by [ConnectivityManager]'s network callbacks.
 * Emits the current reachability immediately, then on every available/lost
 * transition. Requires the `ACCESS_NETWORK_STATE` permission.
 */
class AndroidConnectivityObserver(
  context: Context
) : ConnectivityObserver {
  private val connectivityManager = context.getSystemService(
    Context.CONNECTIVITY_SERVICE
  ) as? ConnectivityManager

  override fun isCurrentlyOnline(): Boolean = connectivityManager?.activeCapabilities().isOnline()

  override fun isCurrentlyMetered(): Boolean = connectivityManager?.activeCapabilities().isMetered()

  override val isOnline: Flow<Boolean>
    get() = capabilities().map { it.isOnline() }.distinctUntilChanged()

  override val isMetered: Flow<Boolean>
    get() = capabilities().map { it.isMetered() }.distinctUntilChanged()

  private fun capabilities(): Flow<NetworkCapabilities?> {
    val manager = connectivityManager ?: return flowOf(null)
    return callbackFlow {
      val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
          trySend(manager.getNetworkCapabilities(network))
        }

        override fun onLost(network: Network) {
          trySend(null)
        }

        override fun onCapabilitiesChanged(
          network: Network,
          capabilities: NetworkCapabilities
        ) {
          trySend(capabilities)
        }
      }

      trySend(manager.activeCapabilities())
      manager.registerDefaultNetworkCallback(callback)
      awaitClose { manager.unregisterNetworkCallback(callback) }
    }
  }

  private fun ConnectivityManager.activeCapabilities(): NetworkCapabilities? =
    activeNetwork?.let { getNetworkCapabilities(it) }

  private fun NetworkCapabilities?.isOnline(): Boolean =
    this != null &&
      hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
      hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

  private fun NetworkCapabilities?.isMetered(): Boolean =
    this != null && !hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
}
