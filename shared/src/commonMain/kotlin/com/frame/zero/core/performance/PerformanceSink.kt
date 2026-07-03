package com.frame.zero.core.performance

/**
 * A self-registering performance-monitoring plugin. Implementations forward custom traces
 * and the collection toggle to a concrete backend (Firebase Performance, …).
 *
 * Register one with `single { MySink() } bind PerformanceSink::class`; [Performance] collects
 * all registered sinks via Koin `getAll()` and fans out to each. To add a new backend,
 * implement this interface and add a single `bind` line — nothing else changes.
 */
interface PerformanceSink {
  /** Starts and returns a backend-specific trace named [name]. */
  fun startTrace(name: String): PerformanceTrace

  /** Enables or disables performance collection for this backend. */
  fun setCollectionEnabled(enabled: Boolean)
}
