package com.frame.zero.core.performance

/**
 * App-wide performance-monitoring facade. Inject this and call it; the registered
 * [PerformanceSink] plugins decide where traces actually go. The facade never throws — a
 * misbehaving sink cannot break reporting for the others.
 */
interface Performance {
  /** Starts a custom trace named [name]; record metrics on it and call [PerformanceTrace.stop]. */
  fun startTrace(name: String): PerformanceTrace

  /** Toggles performance collection across every registered backend. */
  fun setCollectionEnabled(enabled: Boolean)
}

/**
 * Runs [block] inside a trace named [name], stopping it even if [block] throws. Returns
 * whatever [block] returns.
 */
inline fun <T> Performance.trace(
  name: String,
  block: (PerformanceTrace) -> T
): T {
  val trace = startTrace(name)
  try {
    return block(trace)
  } finally {
    trace.stop()
  }
}

/**
 * Fans each call out to every registered [PerformanceSink], isolating each behind
 * `runCatching` so one failing sink can't suppress the rest. [startTrace] returns a composite
 * handle that forwards every metric/stop to each backend's own trace.
 */
class PerformanceImpl(
  private val sinks: List<PerformanceSink>
) : Performance {
  override fun startTrace(name: String): PerformanceTrace =
    CompositePerformanceTrace(sinks.mapNotNull { sink -> runCatching { sink.startTrace(name) }.getOrNull() })

  override fun setCollectionEnabled(enabled: Boolean) {
    sinks.forEach { sink -> runCatching { sink.setCollectionEnabled(enabled) } }
  }
}

private class CompositePerformanceTrace(
  private val traces: List<PerformanceTrace>
) : PerformanceTrace {
  override fun putMetric(
    name: String,
    value: Long
  ) {
    traces.forEach { trace -> runCatching { trace.putMetric(name, value) } }
  }

  override fun incrementMetric(
    name: String,
    by: Long
  ) {
    traces.forEach { trace -> runCatching { trace.incrementMetric(name, by) } }
  }

  override fun stop() {
    traces.forEach { trace -> runCatching { trace.stop() } }
  }
}
