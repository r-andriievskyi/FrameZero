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
  private val userCache: UserCache,
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
    userCache.load()?.let { cachedUser ->
      _state.update { SessionState.LoggedIn(cachedUser) }
    }
    runCatching { authOperations.fetchCurrentUser() }
      .fold(
        onSuccess = { dto ->
          val user = dto.toDomain()
          userCache.save(user)
          _state.update { SessionState.LoggedIn(user) }
        },
        onFailure = { restoreCachedSessionOrLogout() }
      )
  }

  /**
   * A failed [SessionAuthOperations.fetchCurrentUser] is not necessarily a dead
   * session: the auth plugin already clears the tokens when a 401 cannot be
   * recovered by a refresh, so tokens still being present means the failure was
   * transient (offline launch, server error). Keep the session and fall back to
   * the cached profile instead of destroying valid credentials.
   */
  private suspend fun restoreCachedSessionOrLogout() {
    val cachedUser = if (tokenStorage.hasTokens()) userCache.load() else null
    if (cachedUser != null) {
      _state.update { SessionState.LoggedIn(cachedUser) }
    } else {
      forceLogout()
    }
  }

  fun onAuthenticated(user: User) {
    userCache.save(user)
    _state.update { SessionState.LoggedIn(user) }
  }

  suspend fun logout() {
    runCatching { authOperations.signOutRemote() }
    forceLogout()
  }

  private suspend fun forceLogout() {
    cleaners.forEach { cleaner -> runCatching { cleaner.clear() } }
    userCache.clear()
    tokenStorage.clearTokens()
    _state.update { SessionState.LoggedOut }
  }
}
