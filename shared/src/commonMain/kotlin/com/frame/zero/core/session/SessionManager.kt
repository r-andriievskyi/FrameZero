package com.frame.zero.core.session

import com.frame.zero.domain.User
import com.frame.zero.domain.toDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionManager(
  private val tokenStorage: TokenStorage,
  private val authOperations: SessionAuthOperations,
  logoutSignal: LogoutSignal,
  private val cleaners: List<SessionCleaner> = emptyList(),
  scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
  private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
  val state: StateFlow<SessionState> = _state.asStateFlow()

  init {
    scope.launch { logoutSignal.events.collect { forceLogout() } }
  }

  suspend fun initialize() {
    _state.update { SessionState.Loading }
    if (!tokenStorage.hasTokens()) {
      _state.update { SessionState.LoggedOut }
      return
    }
    runCatching { authOperations.fetchCurrentUser() }
      .fold(
        onSuccess = { user -> _state.update { SessionState.LoggedIn(user.toDomain()) } },
        onFailure = { forceLogout() }
      )
  }

  fun onAuthenticated(user: User) {
    _state.update { SessionState.LoggedIn(user) }
  }

  suspend fun logout() {
    runCatching { authOperations.signOutRemote() }
    forceLogout()
  }

  private suspend fun forceLogout() {
    cleaners.forEach { cleaner -> runCatching { cleaner.clear() } }
    tokenStorage.clearTokens()
    _state.update { SessionState.LoggedOut }
  }
}
