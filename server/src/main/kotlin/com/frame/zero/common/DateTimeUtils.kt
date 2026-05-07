package com.frame.zero.common

import java.time.ZoneId
import java.time.LocalDate as JavaLocalDate
import kotlinx.datetime.LocalDate as KotlinLocalDate

fun JavaLocalDate.toKotlin(): KotlinLocalDate = KotlinLocalDate(year, monthValue, dayOfMonth)

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
