package com.frame.zero.core.session

import com.frame.zero.domain.Outcome
import com.frame.zero.domain.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionManager(
  private val tokenStorage: TokenStorage,
  private val authOperations: SessionAuthOperations,
  logoutSignal: LogoutSignal,
  scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
  private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
  val state: StateFlow<SessionState> = _state.asStateFlow()

  init {
    scope.launch { logoutSignal.events.collect { forceLogout() } }
  }

  suspend fun initialize() {
    _state.value = SessionState.Loading
    if (!tokenStorage.hasTokens()) {
      _state.value = SessionState.LoggedOut
      return
    }
    when (val outcome = authOperations.fetchCurrentUser()) {
      is Outcome.Success -> _state.value = SessionState.LoggedIn(outcome.data)
      is Outcome.Failure -> forceLogout()
    }
  }

  fun onAuthenticated(user: User) {
    _state.value = SessionState.LoggedIn(user)
  }

  suspend fun logout() {
    runCatching { authOperations.signOutRemote() }
    forceLogout()
  }

  private fun forceLogout() {
    tokenStorage.clearTokens()
    _state.value = SessionState.LoggedOut
  }
}
