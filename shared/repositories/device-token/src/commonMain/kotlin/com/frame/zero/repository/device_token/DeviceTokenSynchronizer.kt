package com.frame.zero.repository.device_token

import com.frame.zero.core.logging.Logger
import com.frame.zero.core.push.PushTokenProvider
import com.frame.zero.core.session.SessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Keeps the server's record of this device's push token current. Registers the token
 * whenever the session becomes logged-in and re-registers whenever the platform
 * rotates it. Sign-out cleanup is handled separately by [DeviceTokenSessionCleaner].
 *
 * Created eagerly (see the Koin module) so it starts observing at app start; all work
 * is best-effort — failures are logged, never surfaced, since a missing push token
 * must not block using the app.
 */
class DeviceTokenSynchronizer(
  private val sessionState: StateFlow<SessionState>,
  private val tokenProvider: PushTokenProvider,
  private val repository: DeviceTokenRepository,
  private val logger: Logger,
  scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
  init {
    scope.launch {
      sessionState
        .map { it is SessionState.LoggedIn }
        .distinctUntilChanged()
        .filter { loggedIn -> loggedIn }
        .collect { registerCurrentToken() }
    }
  }

  suspend fun onNewToken(token: String) {
    if (sessionState.value is SessionState.LoggedIn) register(token)
  }

  private suspend fun registerCurrentToken() {
    val token = runCatching { tokenProvider.currentToken() }
      .onFailure { logger.w(TAG, "Could not read push token", it) }
      .getOrNull() ?: return
    register(token)
  }

  private suspend fun register(token: String) {
    runCatching { repository.register(token, devicePlatform()) }
      .onFailure { logger.w(TAG, "Failed to register device token", it) }
  }

  private companion object {
    const val TAG = "DeviceTokenSynchronizer"
  }
}
