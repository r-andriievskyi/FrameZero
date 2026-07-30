package com.frame.zero.core.format

/** Whole-dollar USD amount from cents (no cents shown, matching the budget UI's precision),
 *  grouped and symbol-placed per the device locale, e.g. "$1,234" / "1.234 $". */
expect fun formatCurrencyUsdCents(cents: Long): String

/** Fixed one-decimal number per the device locale, e.g. "12.3" / "12,3". */
expect fun formatOneDecimalPlace(value: Double): String

/** Short clock time for the given hour/minute (24-hour input), per the device locale — 12-hour with a meridiem or 24-hour, whichever the locale uses. */
expect fun formatClockTime(
  hour: Int,
  minute: Int
): String

/** Glue between two [formatClockTime] labels in a range, per the device locale. */
expect fun formatTimeRangeSeparator(): String
