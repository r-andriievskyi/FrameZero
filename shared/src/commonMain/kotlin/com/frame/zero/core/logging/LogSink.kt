package com.frame.zero.core.logging

/**
 * A self-registering logging plugin. Implementations receive every log record the app
 * emits through [Logger] and forward it to a concrete backend (console, Logcat/NSLog,
 * Crashlytics, …).
 *
 * Register one with `single { MySink() } bind LogSink::class`; [Logger] collects all
 * registered sinks via Koin `getAll()` and fans out to each. To add a new backend,
 * implement this interface and add a single `bind` line — nothing else changes.
 */
interface LogSink {
  fun log(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?
  )
}
