package com.frame.zero.common

import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.LocalDate as JavaLocalDate
import kotlinx.datetime.LocalDate as KotlinLocalDate
import kotlinx.datetime.number

fun JavaLocalDate.toKotlin(): KotlinLocalDate = KotlinLocalDate(year, monthValue, dayOfMonth)

fun KotlinLocalDate.toJava(): JavaLocalDate = JavaLocalDate.of(year, month.number, day)

fun dueLabelFor(
  date: JavaLocalDate,
  tz: ZoneId
): String {
  val today = JavaLocalDate.now(tz)
  return when (date) {
    today -> "Today"
    today.plusDays(1) -> "Tomorrow"
    else ->
      date.format(
        java.time.format.DateTimeFormatter
          .ofPattern("MMM d")
      )
  }
}

fun computeProgressPercent(
  start: JavaLocalDate,
  wrap: JavaLocalDate,
  today: JavaLocalDate
): Int {
  if (!today.isAfter(start)) return 0
  if (!today.isBefore(wrap)) return 100
  val totalDays = ChronoUnit.DAYS
    .between(start, wrap)
    .coerceAtLeast(1)
  val elapsedDays = ChronoUnit.DAYS
    .between(start, today)
  return (elapsedDays * 100 / totalDays).toInt().coerceIn(0, 100)
}

