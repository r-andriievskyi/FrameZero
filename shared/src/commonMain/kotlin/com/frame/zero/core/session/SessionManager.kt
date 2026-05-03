package com.frame.zero.core.session

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
    runCatching { authOperations.fetchCurrentUser() }
      .fold(
        onSuccess = { _state.value = SessionState.LoggedIn(it.toDomain()) },
        onFailure = { forceLogout() },
      )
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
