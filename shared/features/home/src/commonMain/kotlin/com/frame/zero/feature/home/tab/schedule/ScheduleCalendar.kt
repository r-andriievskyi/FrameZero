package com.frame.zero.feature.home.tab.schedule

import com.frame.zero.domain.schedule.Schedule
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus

// Calendar layout for the Schedule tab's week strip and month grid (Monday-first), shared by the
// Compose and SwiftUI renderers.

/** Monday of the week containing [date]. */
fun weekStartFor(date: LocalDate): LocalDate = date.plus(-daysFromMonday(date.dayOfWeek), DateTimeUnit.DAY)

/** The seven dates of the week containing [date], Monday first. */
fun weekOf(date: LocalDate): List<LocalDate> {
  val start = weekStartFor(date)
  return List(DAYS_PER_WEEK) { start.plus(it, DateTimeUnit.DAY) }
}

/** One month laid out on a Monday-first grid: [leadingBlanks] empty cells, then every day. */
data class MonthGrid(
  val leadingBlanks: Int,
  val days: List<LocalDate>
)

fun monthGrid(
  year: Int,
  month: Month
): MonthGrid {
  val first = LocalDate(year, month, 1)
  val dayCount = (first.plus(1, DateTimeUnit.MONTH).toEpochDays() - first.toEpochDays()).toInt()
  return MonthGrid(
    leadingBlanks = daysFromMonday(first.dayOfWeek),
    days = List(dayCount) { first.plus(it, DateTimeUnit.DAY) }
  )
}

/** Dates that have events or tasks — drive the calendar's dot indicators. */
fun Schedule?.daysWithItems(): Set<LocalDate> =
  this?.days
    ?.filter { it.events.isNotEmpty() || it.tasks.isNotEmpty() }
    ?.mapTo(mutableSetOf()) { it.date }
    .orEmpty()

private const val DAYS_PER_WEEK = 7

private fun daysFromMonday(day: DayOfWeek): Int =
  (day.ordinal - DayOfWeek.MONDAY.ordinal + DAYS_PER_WEEK) % DAYS_PER_WEEK
