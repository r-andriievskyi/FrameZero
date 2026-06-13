package com.frame.zero.integrations.firebase

import com.frame.zero.core.logging.LogLevel
import kotlin.test.Test
import kotlin.test.assertEquals

class CrashlyticsLogMappingTest {
  @Test
  fun `error with throwable records the exception`() {
    assertEquals(
      CrashlyticsAction.BreadcrumbAndRecord,
      crashlyticsActionFor(LogLevel.Error, RuntimeException("boom"))
    )
  }

  @Test
  fun `warn with throwable records the exception`() {
    assertEquals(
      CrashlyticsAction.BreadcrumbAndRecord,
      crashlyticsActionFor(LogLevel.Warn, IllegalStateException())
    )
  }

  @Test
  fun `error without throwable is breadcrumb only`() {
    assertEquals(
      CrashlyticsAction.BreadcrumbOnly,
      crashlyticsActionFor(LogLevel.Error, throwable = null)
    )
  }

  @Test
  fun `info with throwable stays below the record threshold`() {
    assertEquals(
      CrashlyticsAction.BreadcrumbOnly,
      crashlyticsActionFor(LogLevel.Info, RuntimeException("noise"))
    )
  }

  @Test
  fun `breadcrumb is formatted with level and tag`() {
    assertEquals("[Error/Network] timed out", breadcrumbOf(LogLevel.Error, "Network", "timed out"))
  }
}
