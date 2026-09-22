package com.frame.zero.core.logging

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

interface Logger {
  fun v(
    tag: String,
    message: String,
    throwable: Throwable? = null
  )

  fun d(
    tag: String,
    message: String,
    throwable: Throwable? = null
  )

  fun i(
    tag: String,
    message: String,
    throwable: Throwable? = null
  )

  fun w(
    tag: String,
    message: String,
    throwable: Throwable? = null
  )

  fun e(
    tag: String,
    message: String,
    throwable: Throwable? = null
  )
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class LoggerImpl(
  private val sinks: Set<LogSink>
) : Logger {
  override fun v(
    tag: String,
    message: String,
    throwable: Throwable?
  ) = dispatch(LogLevel.Verbose, tag, message, throwable)

  override fun d(
    tag: String,
    message: String,
    throwable: Throwable?
  ) = dispatch(LogLevel.Debug, tag, message, throwable)

  override fun i(
    tag: String,
    message: String,
    throwable: Throwable?
  ) = dispatch(LogLevel.Info, tag, message, throwable)

  override fun w(
    tag: String,
    message: String,
    throwable: Throwable?
  ) = dispatch(LogLevel.Warn, tag, message, throwable)

  override fun e(
    tag: String,
    message: String,
    throwable: Throwable?
  ) = dispatch(LogLevel.Error, tag, message, throwable)

  private fun dispatch(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?
  ) {
    sinks.forEach { sink -> runCatching { sink.log(level, tag, message, throwable) } }
  }
}
