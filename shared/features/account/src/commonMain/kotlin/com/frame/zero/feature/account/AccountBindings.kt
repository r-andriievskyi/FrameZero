package com.frame.zero.feature.account

import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.core.security.AppLockController
import com.frame.zero.core.session.SessionManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

/**
 * [AccountViewModel] is built here rather than constructor-injected because its `isDebug` flag is
 * derived from [NetworkConfig] — a plain `Boolean` is not a meaningful binding to put on the graph.
 */
@ContributesTo(AppScope::class)
@BindingContainer
object AccountBindings {
  @Provides
  fun accountViewModel(
    sessionManager: SessionManager,
    appLockController: AppLockController,
    networkConfig: NetworkConfig
  ): AccountViewModel =
    AccountViewModel(
      sessionManager = sessionManager,
      appLockController = appLockController,
      isDebug = networkConfig.isDebug
    )
}
