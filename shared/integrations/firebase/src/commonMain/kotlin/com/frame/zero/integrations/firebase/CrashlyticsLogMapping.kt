package com.frame.zero.integrations.firebase

import com.frame.zero.core.logging.LogLevel

/**
 * Pure decision for how a single log record maps onto Crashlytics, extracted so it can be
 * unit-tested without a live Firebase instance. Every record becomes a breadcrumb; records
 * that carry a [Throwable] at [LogLevel.Warn] or above are additionally reported as
 * non-fatal exceptions.
 */
internal enum class CrashlyticsAction {
  BreadcrumbOnly,
  BreadcrumbAndRecord
}

internal fun crashlyticsActionFor(
  level: LogLevel,
  throwable: Throwable?
): CrashlyticsAction =
  if (throwable != null && level >= LogLevel.Warn) {
    CrashlyticsAction.BreadcrumbAndRecord
  } else {
    CrashlyticsAction.BreadcrumbOnly
  }

internal fun breadcrumbOf(
  level: LogLevel,
  tag: String,
  message: String
): String = "[${level.name}/$tag] $message"
