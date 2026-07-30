package com.frame.zero.feature.account

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.core.security.AppLockController
import com.frame.zero.core.security.BiometricPromptText
import com.frame.zero.core.security.BiometricResult
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.SessionState
import com.frame.zero.ui.asUiText
import framezero.shared.features.account.generated.resources.Res
import framezero.shared.features.account.generated.resources.error_biometric_failed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AccountViewModel(
  private val sessionManager: SessionManager,
  private val appLockController: AppLockController,
  dispatcher: CoroutineContext = Dispatchers.Main.immediate,
  isDebug: Boolean = false
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(
    AccountState(
      appLockSupported = appLockController.isBiometricAvailable() || appLockController.isEnabled,
      appLockEnabled = appLockController.isEnabled,
      developerOptionsEnabled = isDebug
    )
  )
  val state: StateFlow<AccountState> = _state.asStateFlow()

  init {
    scope.launch {
      sessionManager.state.filterIsInstance<SessionState.LoggedIn>().collect { loggedIn ->
        _state.update {
          it.copy(
            userName = "${loggedIn.user.firstName} ${loggedIn.user.lastName}".trim(),
            email = loggedIn.user.email
          )
        }
      }
    }
  }

  fun onIntent(intent: AccountIntent) {
    when (intent) {
      is AccountIntent.AppLockToggled -> setAppLockEnabled(intent.enabled, intent.prompt)
      AccountIntent.SignOutClicked -> signOut()
      AccountIntent.AppLockErrorDismissed -> _state.update { it.copy(appLockError = null) }
    }
  }

  private fun setAppLockEnabled(
    enabled: Boolean,
    prompt: BiometricPromptText
  ) {
    if (!enabled) {
      appLockController.setEnabled(false)
      _state.update { it.copy(appLockEnabled = false, appLockError = null) }
      return
    }
    scope.launch {
      // Cancelled (user dismissed the prompt) is expected and silent; Error (no hardware,
      // lockout, …) is surfaced — otherwise the toggle just snaps back with no explanation.
      val result = appLockController.authenticate(prompt)
      if (result is BiometricResult.Success) {
        appLockController.setEnabled(true)
      }
      _state.update {
        it.copy(
          appLockEnabled = appLockController.isEnabled,
          appLockError = if (result is BiometricResult.Error) Res.string.error_biometric_failed.asUiText() else null
        )
      }
    }
  }

  private fun signOut() {
    scope.launch { sessionManager.logout() }
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
