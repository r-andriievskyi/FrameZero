package com.frame.zero.core.logging

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

class LoggerImpl(
  private val sinks: List<LogSink>
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
