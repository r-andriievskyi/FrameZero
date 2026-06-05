package com.frame.zero.core.analytics

import kotlin.test.Test
import kotlin.test.assertEquals

private class RecordingSink : AnalyticsSink {
  val events = mutableListOf<AnalyticsEvent>()
  val identities = mutableListOf<String?>()

  override fun track(event: AnalyticsEvent) {
    events += event
  }

  override fun identify(userId: String?) {
    identities += userId
  }
}

private class ThrowingSink : AnalyticsSink {
  override fun track(event: AnalyticsEvent): Nothing = throw IllegalStateException("track boom")

  override fun identify(userId: String?): Nothing = throw IllegalStateException("identify boom")
}

class AnalyticsTest {
  @Test
  fun `fans track and identify out to every registered sink`() {
    val first = RecordingSink()
    val second = RecordingSink()
    val analytics = AnalyticsImpl(listOf(first, second))
    val event = AnalyticsEvent("auth_login", mapOf("method" to "password"))

    analytics.track(event)
    analytics.identify("user-1")

    assertEquals(listOf(event), first.events)
    assertEquals(listOf(event), second.events)
    assertEquals(listOf<String?>("user-1"), first.identities)
    assertEquals(listOf<String?>("user-1"), second.identities)
  }

  @Test
  fun `screen produces a screen_view event carrying the screen name`() {
    val sink = RecordingSink()
    val analytics = AnalyticsImpl(listOf(sink))

    analytics.screen("Home", mapOf("source" to "tab"))

    val event = sink.events.single()
    assertEquals("screen_view", event.name)
    assertEquals(mapOf("source" to "tab", "screen_name" to "Home"), event.params)
  }

  @Test
  fun `a throwing sink does not prevent other sinks from receiving the event`() {
    val healthy = RecordingSink()
    val analytics = AnalyticsImpl(listOf(ThrowingSink(), healthy))

    analytics.track(AnalyticsEvent("still_delivered"))
    analytics.identify("user-2")

    assertEquals("still_delivered", healthy.events.single().name)
    assertEquals(listOf<String?>("user-2"), healthy.identities)
  }
}
