package com.frame.zero.core.format

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char

/** Formats as "Jun 5" — abbreviated month + day. */
private val shortDateFormat = LocalDate.Format {
  monthName(MonthNames.ENGLISH_ABBREVIATED)
  char(' ')
  day()
}

/** Formats as "Jun 5, 2026" — abbreviated month + day + year. */
private val mediumDateFormat = LocalDate.Format {
  monthName(MonthNames.ENGLISH_ABBREVIATED)
  char(' ')
  day()
  chars(", ")
  year()
}

/**
 * Short date label, e.g. "Jun 5".
 */
fun LocalDate.formatShort(): String = shortDateFormat.format(this)

/**
 * Medium date label, e.g. "Jun 5, 2026".
 */
fun LocalDate.formatMedium(): String = mediumDateFormat.format(this)

/**
 * Abbreviated month name, e.g. "Jun".
 */
fun LocalDate.formatMonthShort(): String = MonthNames.ENGLISH_ABBREVIATED.names[month.ordinal]

/**
 * Abbreviated day-of-week name, e.g. "Mon".
 */
fun DayOfWeek.shortName(): String = DayOfWeekNames.ENGLISH_ABBREVIATED.names[ordinal]

/**
 * Full day-of-week name, e.g. "Monday".
 */
fun DayOfWeek.fullName(): String = DayOfWeekNames.ENGLISH_FULL.names[ordinal]

/**
 * Full month name, e.g. "January".
 */
fun Month.fullName(): String = MonthNames.ENGLISH_FULL.names[ordinal]

/**
 * Single-letter calendar column headers (Mon→"M", Tue→"T", …).
 */
val calendarDayLabels: List<String> =
  DayOfWeekNames.ENGLISH_ABBREVIATED.names.map { it.first().toString() }
