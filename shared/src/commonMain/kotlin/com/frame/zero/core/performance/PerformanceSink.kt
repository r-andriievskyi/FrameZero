package com.frame.zero.core.performance

/**
 * A self-registering performance-monitoring plugin. Implementations forward custom traces
 * and the collection toggle to a concrete backend (Firebase Performance, …).
 *
 * Annotate an implementation `@ContributesIntoSet(AppScope::class)`; [Performance] takes the
 * resulting `Set<PerformanceSink>` and fans out to each. To add a new backend, implement this
 * interface and add that one annotation — nothing else changes.
 */
interface PerformanceSink {
  /** Starts and returns a backend-specific trace named [name]. */
  fun startTrace(name: String): PerformanceTrace

  /** Enables or disables performance collection for this backend. */
  fun setCollectionEnabled(enabled: Boolean)
}
