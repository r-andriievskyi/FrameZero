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
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AccountViewModel(
  private val sessionManager: SessionManager,
  dispatcher: CoroutineContext = Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val user = (sessionManager.state.value as? SessionState.LoggedIn)?.user

  private val _state = MutableStateFlow(
    AccountState(
      userName = user?.let { "${it.firstName} ${it.lastName}".trim() }.orEmpty(),
      email = user?.email.orEmpty()
    )
  )
  val state: StateFlow<AccountState> = _state.asStateFlow()

  fun signOut() {
    scope.launch { sessionManager.logout() }
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
