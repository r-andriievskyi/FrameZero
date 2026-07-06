package com.frame.zero.domain.schedule

import kotlinx.serialization.Serializable

@Serializable
enum class ScheduleEventKind {
  SHOOT,
  MEETING,
  REVIEW,
  OTHER
}
