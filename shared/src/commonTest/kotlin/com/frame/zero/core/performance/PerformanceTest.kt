package com.frame.zero.core.performance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class RecordingTrace : PerformanceTrace {
  val metrics = mutableMapOf<String, Long>()
  var stopped = false

  override fun putMetric(
    name: String,
    value: Long
  ) {
    metrics[name] = value
  }

  override fun incrementMetric(
    name: String,
    by: Long
  ) {
    metrics[name] = (metrics[name] ?: 0L) + by
  }

  override fun stop() {
    stopped = true
  }
}

private class RecordingSink : PerformanceSink {
  val traces = mutableListOf<Pair<String, RecordingTrace>>()
  val collectionToggles = mutableListOf<Boolean>()

  override fun startTrace(name: String): PerformanceTrace = RecordingTrace().also { traces += name to it }

  override fun setCollectionEnabled(enabled: Boolean) {
    collectionToggles += enabled
  }
}

private class ThrowingSink : PerformanceSink {
  override fun startTrace(name: String): Nothing = throw IllegalStateException("startTrace boom")

  override fun setCollectionEnabled(enabled: Boolean): Nothing = throw IllegalStateException("toggle boom")
}

class PerformanceTest {
  @Test
  fun `startTrace fans metrics and stop out to every registered sink`() {
    val first = RecordingSink()
    val second = RecordingSink()
    val performance = PerformanceImpl(listOf(first, second))

    val trace = performance.startTrace("checkout")
    trace.putMetric("items", 3)
    trace.incrementMetric("retries")
    trace.stop()

    listOf(first, second).forEach { sink ->
      val (name, recorded) = sink.traces.single()
      assertEquals("checkout", name)
      assertEquals(mapOf("items" to 3L, "retries" to 1L), recorded.metrics)
      assertTrue(recorded.stopped)
    }
  }

  @Test
  fun `setCollectionEnabled fans out to every registered sink`() {
    val sink = RecordingSink()
    val performance = PerformanceImpl(listOf(sink))

    performance.setCollectionEnabled(false)

    assertEquals(listOf(false), sink.collectionToggles)
  }

  @Test
  fun `trace helper stops the trace even when the block throws`() {
    val sink = RecordingSink()
    val performance = PerformanceImpl(listOf(sink))

    runCatching { performance.trace<Unit>("boom") { error("inside") } }

    assertTrue(sink.traces.single().second.stopped)
  }

  @Test
  fun `a throwing sink does not prevent other sinks from receiving the trace`() {
    val healthy = RecordingSink()
    val performance = PerformanceImpl(listOf(ThrowingSink(), healthy))

    val trace = performance.startTrace("still_traced")
    trace.putMetric("value", 7)
    trace.stop()
    performance.setCollectionEnabled(true)

    val (name, recorded) = healthy.traces.single()
    assertEquals("still_traced", name)
    assertEquals(mapOf("value" to 7L), recorded.metrics)
    assertTrue(recorded.stopped)
    assertEquals(listOf(true), healthy.collectionToggles)
  }
}
