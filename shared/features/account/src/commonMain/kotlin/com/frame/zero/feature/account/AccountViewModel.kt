package com.frame.zero.feature.account

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.SessionState
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
  dispatcher: CoroutineContext = Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(AccountState())
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

  fun signOut() {
    scope.launch { sessionManager.logout() }
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
