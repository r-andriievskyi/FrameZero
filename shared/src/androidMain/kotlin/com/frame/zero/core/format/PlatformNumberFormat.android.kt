package com.frame.zero.core.format

import java.text.DateFormat
import java.text.NumberFormat
import java.util.Calendar
import java.util.Currency
import java.util.Locale

actual fun formatCurrencyUsdCents(cents: Long): String {
  val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
    currency = Currency.getInstance("USD")
    minimumFractionDigits = 0
    maximumFractionDigits = 0
  }
  return formatter.format(cents / 100)
}

actual fun formatOneDecimalPlace(value: Double): String {
  val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
    minimumFractionDigits = 1
    maximumFractionDigits = 1
  }
  return formatter.format(value)
}

actual fun formatClockTime(hour: Int, minute: Int): String {
  val time = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, hour)
    set(Calendar.MINUTE, minute)
  }.time
  return DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(time)
}

actual fun formatTimeRangeSeparator(): String = " – "
