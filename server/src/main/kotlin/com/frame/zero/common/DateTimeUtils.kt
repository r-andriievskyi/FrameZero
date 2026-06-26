package com.frame.zero.common

import com.frame.zero.domain.production.ProductionPhase
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Instant

private const val NANOS_PER_MICRO = 1_000

private val PHASE_PROGRESS_FLOOR: Map<ProductionPhase, Int> = mapOf(
  ProductionPhase.IDEA to 0,
  ProductionPhase.DEVELOPMENT to 8,
  ProductionPhase.FINANCING to 18,
  ProductionPhase.PRE_PRODUCTION to 30,
  ProductionPhase.PRODUCTION to 50,
  ProductionPhase.POST_PRODUCTION to 72,
  ProductionPhase.MARKETING to 85,
  ProductionPhase.DISTRIBUTION to 92,
  ProductionPhase.RELEASE to 100,
  ProductionPhase.ARCHIVED to 100
)

/** Floor of the next phase (where this phase's band ends), or 100 for the last. */
private fun phaseCeiling(phase: ProductionPhase): Int =
  ProductionPhase.entries.getOrNull(phase.ordinal + 1)
    ?.let { PHASE_PROGRESS_FLOOR.getValue(it) }
    ?: 100

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
 * Lifecycle progress for a production, driven by its [phase] and refined by how
 * far the [start]..[wrap] schedule has elapsed as of [today]. The phase sets the
 * band ([PHASE_PROGRESS_FLOOR] up to the next phase's floor); the elapsed
 * fraction of the calendar window positions the production within that band, so
 * two productions in the same phase still differ by schedule. The result never
 * exceeds the phase's ceiling, so e.g. post-production cannot read as 100%.
 */
fun computeProgressPercent(
  phase: ProductionPhase,
  start: LocalDate,
  wrap: LocalDate,
  today: LocalDate
): Int {
  val floor = PHASE_PROGRESS_FLOOR.getValue(phase)
  val ceiling = phaseCeiling(phase)
  if (floor >= ceiling) return floor
  val windowFraction = when {
    today <= start -> 0.0
    today >= wrap -> 1.0
    else -> start.daysUntil(today).toDouble() / start.daysUntil(wrap).coerceAtLeast(1)
  }
  return (floor + (ceiling - floor) * windowFraction).roundToInt().coerceIn(floor, ceiling)
}
