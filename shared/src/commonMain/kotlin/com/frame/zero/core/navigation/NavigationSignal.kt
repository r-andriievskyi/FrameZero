package com.frame.zero.core.navigation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Bridge from platform entry points (e.g. a tapped push notification on Android) to
 * the Decompose root, mirroring [com.frame.zero.core.session.LogoutSignal]. The root
 * collects [events] and navigates; if the session isn't logged in yet it buffers the
 * deep link and replays it once login completes.
 */
class NavigationSignal {
  // replay = 1 so a deep link emitted at cold start (before the root subscribes) is
  // still delivered once it does. [consume] clears it after handling so it isn't
  // re-delivered to a fresh collector after a config change.
  private val _events = MutableSharedFlow<DeepLink>(replay = 1, extraBufferCapacity = 1)
  val events: SharedFlow<DeepLink> = _events.asSharedFlow()

  fun emit(deepLink: DeepLink) {
    _events.tryEmit(deepLink)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  fun consume() {
    _events.resetReplayCache()
  }
}

sealed interface DeepLink {
  data class TaskDetails(
    val taskId: String
  ) : DeepLink
}
