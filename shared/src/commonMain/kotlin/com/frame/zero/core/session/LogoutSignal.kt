package com.frame.zero.core.session

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Bridge between the network layer and [SessionManager]: emitted when refreshing the access token
 * fails so that the session can transition to [SessionState.LoggedOut].
 */
@SingleIn(AppScope::class)
@Inject
class LogoutSignal {
  private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  val events: SharedFlow<Unit> = _events.asSharedFlow()

  fun emit() {
    _events.tryEmit(Unit)
  }
}
