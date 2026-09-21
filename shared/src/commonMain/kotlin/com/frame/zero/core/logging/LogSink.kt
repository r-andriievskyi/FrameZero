package com.frame.zero.core.logging

/**
 * A self-registering logging plugin. Implementations receive every log record the app
 * emits through [Logger] and forward it to a concrete backend (console, Logcat/NSLog,
 * Crashlytics, …).
 *
 * Annotate an implementation `@ContributesIntoSet(AppScope::class)`; [Logger] takes the
 * resulting `Set<LogSink>` and fans out to each. To add a new backend, implement this
 * interface and add that one annotation — nothing else changes, and a graph that cannot
 * see the contribution fails to compile rather than silently dropping it.
 */
interface LogSink {
  fun log(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?
  )
}
