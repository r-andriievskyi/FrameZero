package com.frame.zero.core.analytics

/**
 * App-wide analytics facade. Inject this and call it; the registered [AnalyticsSink]
 * plugins decide where events actually go. The facade never throws — a misbehaving sink
 * cannot break reporting for the others.
 */
interface Analytics {
  fun track(event: AnalyticsEvent)

  fun screen(
    name: String,
    params: Map<String, String> = emptyMap()
  )

  fun identify(userId: String?)
}

/**
 * Fans each call out to every registered [AnalyticsSink], isolating each behind
 * `runCatching` so one failing sink can't suppress the rest. [screen] is sugar over
 * [track] using a conventional `screen_view` event.
 */
class AnalyticsImpl(
  private val sinks: List<AnalyticsSink>
) : Analytics {
  override fun track(event: AnalyticsEvent) {
    sinks.forEach { sink -> runCatching { sink.track(event) } }
  }

  override fun screen(
    name: String,
    params: Map<String, String>
  ) {
    track(AnalyticsEvent(name = SCREEN_VIEW_EVENT, params = params + (SCREEN_NAME_PARAM to name)))
  }

  override fun identify(userId: String?) {
    sinks.forEach { sink -> runCatching { sink.identify(userId) } }
  }

  private companion object {
    const val SCREEN_VIEW_EVENT = "screen_view"
    const val SCREEN_NAME_PARAM = "screen_name"
  }
}
