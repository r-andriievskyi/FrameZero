package com.frame.zero.core.analytics

/**
 * Placeholder [AnalyticsSink] that discards every event. It exists so the analytics
 * plugin is fully wired and resolvable end to end while a real backend is still pending.
 *
 * Replace (or sit alongside) this with a concrete sink when ready — e.g. Firebase,
 * Amplitude, or one that routes events through `Logger` — by adding another
 * `single { … } bind AnalyticsSink::class` line in [analyticsModule].
 */
class NoOpAnalyticsSink : AnalyticsSink {
  override fun track(event: AnalyticsEvent) = Unit

  override fun identify(userId: String?) = Unit
}
