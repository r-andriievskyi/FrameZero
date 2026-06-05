package com.frame.zero.core.logging

import kotlin.test.Test
import kotlin.test.assertEquals

private data class Record(
  val level: LogLevel,
  val tag: String,
  val message: String,
  val throwable: Throwable?
)

private class RecordingSink : LogSink {
  val records = mutableListOf<Record>()

  override fun log(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?
  ) {
    records += Record(level, tag, message, throwable)
  }
}

private class ThrowingSink : LogSink {
  override fun log(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?
  ): Nothing = throw IllegalStateException("sink boom")
}

class LoggerTest {
  @Test
  fun `dispatches each record to every registered sink`() {
    val first = RecordingSink()
    val second = RecordingSink()
    val logger = LoggerImpl(listOf(first, second))

    logger.i("Net", "hello")

    val expected = Record(LogLevel.Info, "Net", "hello", null)
    assertEquals(listOf(expected), first.records)
    assertEquals(listOf(expected), second.records)
  }

  @Test
  fun `maps each facade method to its level and forwards the throwable`() {
    val sink = RecordingSink()
    val logger = LoggerImpl(listOf(sink))
    val cause = RuntimeException("cause")

    logger.v("T", "v")
    logger.d("T", "d")
    logger.i("T", "i")
    logger.w("T", "w")
    logger.e("T", "e", cause)

    assertEquals(
      listOf(LogLevel.Verbose, LogLevel.Debug, LogLevel.Info, LogLevel.Warn, LogLevel.Error),
      sink.records.map { it.level }
    )
    assertEquals(cause, sink.records.last().throwable)
  }

  @Test
  fun `a throwing sink does not prevent other sinks from receiving the record`() {
    val healthy = RecordingSink()
    val logger = LoggerImpl(listOf(ThrowingSink(), healthy))

    logger.e("T", "still delivered")

    assertEquals(1, healthy.records.size)
    assertEquals("still delivered", healthy.records.single().message)
  }
}
