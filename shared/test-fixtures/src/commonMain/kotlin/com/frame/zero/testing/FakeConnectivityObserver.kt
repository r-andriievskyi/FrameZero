package com.frame.zero.testing

import com.frame.zero.core.network.connectivity.ConnectivityObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Controllable [ConnectivityObserver] for tests. Flip [online] to simulate
 * connectivity changes and assert auto-reload behaviour.
 */
class FakeConnectivityObserver(
  initiallyOnline: Boolean = true
) : ConnectivityObserver {
  val online: MutableStateFlow<Boolean> = MutableStateFlow(initiallyOnline)
  override val isOnline: Flow<Boolean> = online

  override fun isCurrentlyOnline(): Boolean = online.value
}
