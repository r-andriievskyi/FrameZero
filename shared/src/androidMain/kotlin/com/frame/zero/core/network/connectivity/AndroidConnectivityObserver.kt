package com.frame.zero.core.network.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
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

  override fun isCurrentlyOnline(): Boolean =
    connectivityManager?.hasValidatedInternet() ?: false

  override val isOnline: Flow<Boolean>
    get() {
      val manager = connectivityManager ?: return flowOf(false)
      return callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
          override fun onAvailable(network: Network) {
            trySend(true)
          }

          override fun onLost(network: Network) {
            trySend(manager.hasValidatedInternet())
          }

          override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities
          ) {
            trySend(capabilities.hasValidatedInternet())
          }
        }

        // Seed with the current state so collectors get a value immediately.
        trySend(manager.hasValidatedInternet())
        manager.registerDefaultNetworkCallback(callback)
        awaitClose { manager.unregisterNetworkCallback(callback) }
      }.distinctUntilChanged()
    }

  private fun ConnectivityManager.hasValidatedInternet(): Boolean {
    val network = activeNetwork ?: return false
    val capabilities = getNetworkCapabilities(network) ?: return false
    return capabilities.hasValidatedInternet()
  }

  private fun NetworkCapabilities.hasValidatedInternet(): Boolean =
    hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
      hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
