package com.frame.zero.core.logging

/**
 * Placeholder [LogSink] that discards every record. It exists so the logging plugin is
 * fully wired and resolvable end to end while a real backend is still pending.
 *
 * Replace (or sit alongside) this with a concrete sink when ready — e.g. a `println`
 * sink, a platform-native Logcat/NSLog sink via `expect`/`actual`, or Crashlytics — by
 * adding another `single { … } bind LogSink::class` line in [loggingModule].
 */
class NoOpLogSink : LogSink {
  override fun log(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?
  ) = Unit
}
