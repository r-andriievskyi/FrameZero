package com.frame.zero.testing

import com.frame.zero.core.network.connectivity.ConnectivityObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Controllable [ConnectivityObserver] for tests. Flip [online] to simulate
 * connectivity changes and assert auto-reload behaviour.
 */
class FakeConnectivityObserver(
  initiallyOnline: Boolean = true,
  initiallyMetered: Boolean = false
) : ConnectivityObserver {
  val online: MutableStateFlow<Boolean> = MutableStateFlow(initiallyOnline)
  override val isOnline: Flow<Boolean> = online

  override fun isCurrentlyOnline(): Boolean = online.value

  /** Flip to simulate moving on/off a metered (cellular) connection. */
  val metered: MutableStateFlow<Boolean> = MutableStateFlow(initiallyMetered)
  override val isMetered: Flow<Boolean> = metered

  override fun isCurrentlyMetered(): Boolean = metered.value
}
