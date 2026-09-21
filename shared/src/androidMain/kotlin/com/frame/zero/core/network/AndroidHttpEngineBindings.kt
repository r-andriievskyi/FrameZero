package com.frame.zero.core.network

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit

private const val CONNECT_TIMEOUT_SECONDS = 15L
private const val READ_TIMEOUT_SECONDS = 30L

/**
 * The OkHttp engine, plus whatever debug interceptors the app module chose to link. The
 * interceptor set is a graph *input* rather than a multibinding: only `:androidApp` has real AGP
 * build types, and it sits downstream of the module that declares the graph, so its contributions
 * could never be aggregated here.
 */
@ContributesTo(AppScope::class)
@BindingContainer
object AndroidHttpEngineBindings {
  @Provides
  @SingleIn(AppScope::class)
  fun engine(debugInterceptors: Set<HttpDebugInterceptor>): HttpClientEngine =
    OkHttp.create {
      config {
        connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
      }
      debugInterceptors.forEach { addInterceptor(it) }
    }
}
