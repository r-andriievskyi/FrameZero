package com.frame.zero.domain.task

import kotlinx.serialization.Serializable

@Serializable
enum class TaskPriority {
  HIGH,
  MEDIUM,
  LOW
}
