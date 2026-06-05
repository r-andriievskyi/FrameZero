package com.frame.zero.core.analytics

data class AnalyticsEvent(
  val name: String,
  val params: Map<String, String> = emptyMap()
)
