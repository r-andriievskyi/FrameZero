package com.frame.zero.core.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Bridge between the network layer and [SessionManager]: emitted when refreshing the access token
 * fails so that the session can transition to [SessionState.LoggedOut].
 */
class LogoutSignal {
  private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  val events: SharedFlow<Unit> = _events.asSharedFlow()

  fun emit() {
    _events.tryEmit(Unit)
  }
}
