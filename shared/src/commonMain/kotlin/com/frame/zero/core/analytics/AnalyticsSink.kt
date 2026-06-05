package com.frame.zero.core.analytics

/**
 * A self-registering analytics plugin. Implementations receive every event the app
 * reports through [Analytics] and forward it to a concrete backend (Firebase, Amplitude,
 * a logging sink, …).
 *
 * Register one with `single { MySink() } bind AnalyticsSink::class`; [Analytics] collects
 * all registered sinks via Koin `getAll()` and fans out to each. To add a new backend,
 * implement this interface and add a single `bind` line — nothing else changes.
 */
interface AnalyticsSink {
  fun track(event: AnalyticsEvent)

  fun identify(userId: String?)
}
