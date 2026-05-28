package com.frame.zero.feature.account

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.SessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccountViewModel(
  sessionManager: SessionManager
) : InstanceKeeper.Instance {
  private val user = (sessionManager.state.value as? SessionState.LoggedIn)?.user

  private val _state = MutableStateFlow(
    AccountState(
      userName = user?.let { "${it.firstName} ${it.lastName}".trim() }.orEmpty(),
      email = user?.email.orEmpty()
    )
  )
  val state: StateFlow<AccountState> = _state.asStateFlow()

  override fun onDestroy() = Unit
}
