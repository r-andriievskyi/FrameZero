package com.frame.zero.dto.notification

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class NotificationDto(
  val id: String,
  val title: String,
  val body: String?,
  val readAt: Instant?,
  val createdAt: Instant,
)

@Serializable
data class NotificationsResponse(
  val items: List<NotificationDto>,
  val unreadCount: Int,
  val nextCursor: String?,
)

@Serializable
data class MarkReadRequest(
  val ids: List<String> = emptyList(),
  val all: Boolean = false,
)
