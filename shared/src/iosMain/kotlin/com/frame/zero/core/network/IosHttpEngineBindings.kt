package com.frame.zero.core.network

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

private const val TIMEOUT_SECONDS = 30.0

@ContributesTo(AppScope::class)
@BindingContainer
object IosHttpEngineBindings {
  @Provides
  @SingleIn(AppScope::class)
  fun engine(): HttpClientEngine =
    Darwin.create {
      configureRequest {
        setAllowsCellularAccess(true)
        setTimeoutInterval(TIMEOUT_SECONDS)
      }
    }
}
