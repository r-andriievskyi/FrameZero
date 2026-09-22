package com.frame.zero.integrations.firebase

import com.frame.zero.core.analytics.AnalyticsSink
import com.frame.zero.core.logging.LogSink
import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.core.performance.PerformanceSink
import com.frame.zero.core.push.PushTokenProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * Every Firebase-backed binding in one container, so a graph that has no Firebase at all — the
 * demo flavor runs fully offline — removes them with a single `excludes` entry rather than
 * naming each sink.
 */
@ContributesTo(AppScope::class)
@BindingContainer
object FirebaseBindings {
  @Provides
  @IntoSet
  @SingleIn(AppScope::class)
  fun analyticsSink(): AnalyticsSink = FirebaseAnalyticsSink()

  @Provides
  @IntoSet
  @SingleIn(AppScope::class)
  fun logSink(networkConfig: NetworkConfig): LogSink =
    FirebaseCrashlyticsLogSink(collectionEnabled = !networkConfig.isDebug)

  @Provides
  @IntoSet
  @SingleIn(AppScope::class)
  fun performanceSink(): PerformanceSink = FirebasePerformanceSink()

  @Provides
  @SingleIn(AppScope::class)
  fun pushTokenProvider(): PushTokenProvider = firebasePushTokenProvider()
}
