package com.frame.zero.core.format

import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.currentLocale

actual fun formatCurrencyUsdCents(cents: Long): String {
  val dollars = cents / 100
  val formatter = NSNumberFormatter().apply {
    numberStyle = NSNumberFormatterCurrencyStyle
    locale = NSLocale.currentLocale
    currencyCode = "USD"
    minimumFractionDigits = 0uL
    maximumFractionDigits = 0uL
  }
  return formatter.stringFromNumber(NSNumber(long = dollars)) ?: "$$dollars"
}

actual fun formatOneDecimalPlace(value: Double): String {
  val formatter = NSNumberFormatter().apply {
    numberStyle = NSNumberFormatterDecimalStyle
    locale = NSLocale.currentLocale
    minimumFractionDigits = 1uL
    maximumFractionDigits = 1uL
  }
  return formatter.stringFromNumber(NSNumber(double = value)) ?: value.toString()
}

actual fun formatClockTime(
  hour: Int,
  minute: Int
): String {
  val components = NSDateComponents().apply {
    this.hour = hour.toLong()
    this.minute = minute.toLong()
  }
  val date = NSCalendar.currentCalendar.dateFromComponents(components) ?: NSDate()
  val formatter = NSDateFormatter().apply {
    dateStyle = NSDateFormatterNoStyle
    timeStyle = NSDateFormatterShortStyle
    locale = NSLocale.currentLocale
  }
  return formatter.stringFromDate(date)
}

actual fun formatTimeRangeSeparator(): String = " – "
