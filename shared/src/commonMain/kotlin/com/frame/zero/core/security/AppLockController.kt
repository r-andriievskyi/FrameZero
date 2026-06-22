package com.frame.zero.core.security

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex

/**
 * Owns the biometric app-lock policy: whether the lock is enabled (persisted) and the
 * live [AppLockState]. Pure logic — the actual prompt is delegated to
 * [BiometricAuthenticator], so this is fully testable with a fake.
 *
 * The lock gates the app *after* sign-in (protecting already-loaded sensitive data); it
 * is independent of [com.frame.zero.core.session.SessionManager]. The gate that combines
 * "locked" with "logged in" lives in `RootComponent`.
 *
 * Lifecycle:
 * - Starts [AppLockState.Locked] when the feature is enabled (so a cold start with an
 *   existing session requires biometrics), otherwise [AppLockState.Unlocked].
 * - [onBackgrounded] re-locks when the app leaves the foreground.
 * - [authenticate] unlocks on a successful prompt.
 */
class AppLockController(
  private val authenticator: BiometricAuthenticator,
  private val settings: Settings
) {
  private val _enabled = MutableStateFlow(settings.getBoolean(KEY_ENABLED, false))
  val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

  private val _lockState = MutableStateFlow(
    if (_enabled.value) AppLockState.Locked else AppLockState.Unlocked
  )
  val lockState: StateFlow<AppLockState> = _lockState.asStateFlow()

  private val authGate = Mutex()

  /** Whether the user has turned the biometric lock on. */
  val isEnabled: Boolean
    get() = _enabled.value

  /** Whether the device can actually prompt for biometrics. */
  fun isBiometricAvailable(): Boolean = authenticator.availability() == BiometricAvailability.Available

  /**
   * Turns the lock on or off. Enabling presumes the caller has just run a successful
   * [authenticate] to confirm the hardware works; either way the user stays unlocked in
   * the current session.
   */
  fun setEnabled(enabled: Boolean) {
    settings.putBoolean(KEY_ENABLED, enabled)
    _enabled.update { enabled }
    _lockState.update { AppLockState.Unlocked }
  }

  fun onBackgrounded() {
    if (_enabled.value) _lockState.update { AppLockState.Locked }
  }

  suspend fun authenticate(prompt: BiometricPromptText): BiometricResult {
    if (!authGate.tryLock()) return BiometricResult.Cancelled
    try {
      val result = authenticator.authenticate(prompt)
      if (result is BiometricResult.Success) _lockState.update { AppLockState.Unlocked }
      return result
    } finally {
      authGate.unlock()
    }
  }

  private companion object {
    const val KEY_ENABLED = "security.app_lock_enabled"
  }
}

enum class AppLockState {
  Locked,
  Unlocked
}
