package com.frame.zero.core.performance

/**
 * A running performance trace. Obtain one from [Performance.startTrace], record custom
 * metrics on it, then call [stop] to report its duration to every backend.
 *
 * Implementations never throw — a misbehaving backend cannot break the others.
 */
interface PerformanceTrace {
  /** Sets the metric [name] to [value], creating it if absent. */
  fun putMetric(
    name: String,
    value: Long
  )

  /** Atomically increments the metric [name] by [by] (default 1), creating it if absent. */
  fun incrementMetric(
    name: String,
    by: Long = 1
  )

  /** Stops the trace and reports it. Further calls are no-ops. */
  fun stop()
}
