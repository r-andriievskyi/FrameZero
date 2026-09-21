package com.frame.zero.core.analytics

/**
 * A self-registering analytics plugin. Implementations receive every event the app
 * reports through [Analytics] and forward it to a concrete backend (Firebase, Amplitude,
 * a logging sink, …).
 *
 * Annotate an implementation `@ContributesIntoSet(AppScope::class)`; [Analytics] takes the
 * resulting `Set<AnalyticsSink>` and fans out to each. To add a new backend, implement this
 * interface and add that one annotation — nothing else changes.
 */
interface AnalyticsSink {
  fun track(event: AnalyticsEvent)

  fun identify(userId: String?)
}
