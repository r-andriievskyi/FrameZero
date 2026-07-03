package com.frame.zero.integrations.firebase

import com.frame.zero.core.performance.PerformanceSink
import com.frame.zero.core.performance.PerformanceTrace
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.perf.metrics.Trace
import dev.gitlive.firebase.perf.performance

/**
 * Forwards custom traces to Firebase Performance Monitoring. Automatic traces (app start,
 * screen rendering, network requests) are instrumented by the Firebase Performance Gradle
 * plugin and SDK and need no code here.
 *
 * Whether collection is on by default is decided at process start by the SDK, from the
 * `firebase_performance_collection_enabled` manifest flag on Android (wired to `false` for
 * debug builds) and the matching Info.plist key on iOS — early enough to also gate the
 * automatic traces that fire before any Kotlin runs. [setCollectionEnabled] only layers a
 * runtime override (e.g. a user opt-out) on top of that default.
 */
class FirebasePerformanceSink : PerformanceSink {
  private val performance = Firebase.performance

  override fun startTrace(name: String): PerformanceTrace =
    FirebasePerformanceTrace(performance.newTrace(name).apply { start() })

  override fun setCollectionEnabled(enabled: Boolean) {
    performance.setPerformanceCollectionEnabled(enabled)
  }
}

private class FirebasePerformanceTrace(
  private val trace: Trace
) : PerformanceTrace {
  override fun putMetric(
    name: String,
    value: Long
  ) = trace.putMetric(name, value)

  override fun incrementMetric(
    name: String,
    by: Long
  ) = trace.incrementMetric(name, by)

  override fun stop() = trace.stop()
}
