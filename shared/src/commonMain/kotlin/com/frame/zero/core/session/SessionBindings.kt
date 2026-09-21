package com.frame.zero.core.session

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow

/**
 * Exposes the session's observable state on its own so collaborators that only watch it (e.g.
 * `DeviceTokenSynchronizer`) can depend on the flow rather than on all of [SessionManager] —
 * and stay testable with a plain `MutableStateFlow`.
 */
@ContributesTo(AppScope::class)
@BindingContainer
object SessionBindings {
  @Provides
  fun sessionState(sessionManager: SessionManager): StateFlow<SessionState> = sessionManager.state
}
