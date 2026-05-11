package com.frame.zero.notification

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.dto.notification.MarkReadRequest
import com.frame.zero.dto.notification.NotificationDto
import com.frame.zero.dto.notification.NotificationsResponse
import java.util.UUID
import kotlin.time.toKotlinInstant

class NotificationService(
  private val notifications: NotificationRepository
) {
  suspend fun list(
    userId: UUID,
    limit: Int,
    cursor: String?
  ): NotificationsResponse {
    val (items, nextCursor) = notifications.findForUser(userId, limit, cursor)
    val unread = notifications.countUnread(userId)
    return NotificationsResponse(
      items =
        items.map { n ->
          NotificationDto(
            id = n.id.toString(),
            title = n.title,
            body = n.body,
            readAt = n.readAt?.toKotlinInstant(),
            createdAt = n.createdAt.toKotlinInstant()
          )
        },
      unreadCount = unread,
      nextCursor = nextCursor
    )
  }

  suspend fun markRead(
    userId: UUID,
    request: MarkReadRequest
  ) {
    if (request.all) {
      notifications.markAllRead(userId)
      return
    }
    if (request.ids.isEmpty()) {
      throw AppException(AppError.ValidationError(mapOf("ids" to "Provide ids or set all=true")))
    }
    val uuids =
      request.ids.map {
        runCatching { UUID.fromString(it) }.getOrNull()
          ?: throw AppException(AppError.ValidationError(mapOf("ids" to "Invalid UUID: $it")))
      }
    notifications.markRead(userId, uuids)
  }
}
