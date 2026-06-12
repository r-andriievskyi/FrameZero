package com.frame.zero.common

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlin.time.Clock
import kotlin.time.Instant

private const val NANOS_PER_MICRO = 1_000

/**
 * Current instant truncated to microsecond precision — the precision of a
 * Postgres `timestamp` column. Use this (never raw [Clock.System.now]) for any
 * instant that is persisted or compared against persisted values, so in-memory
 * and round-tripped timestamps stay equal.
 */
fun nowTruncatedToMicros(): Instant {
  val now = Clock.System.now()
  return Instant.fromEpochSeconds(
    now.epochSeconds,
    now.nanosecondsOfSecond / NANOS_PER_MICRO * NANOS_PER_MICRO
  )
}

/**
 * Percentage of a production's [start]..[wrap] window elapsed as of [today],
 * clamped to 0..100. Returns 0 before the window starts and 100 once it ends.
 */
fun computeProgressPercent(
  start: LocalDate,
  wrap: LocalDate,
  today: LocalDate
): Int {
  if (today <= start) return 0
  if (today >= wrap) return 100
  val totalDays = start.daysUntil(wrap).coerceAtLeast(1)
  val elapsedDays = start.daysUntil(today)
  return (elapsedDays.toLong() * 100 / totalDays).toInt().coerceIn(0, 100)
}
