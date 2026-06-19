package com.frame.zero.notification

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.common.Transactor
import com.frame.zero.common.parseUuidField
import com.frame.zero.dto.notification.MarkReadRequest
import com.frame.zero.dto.notification.NotificationDto
import com.frame.zero.dto.notification.NotificationsResponse
import java.util.UUID

class NotificationService(
  private val notifications: NotificationRepository,
  private val transactor: Transactor
) {
  suspend fun list(
    userId: UUID,
    limit: Int,
    cursor: String?
  ): NotificationsResponse =
    transactor.transaction {
      val (items, nextCursor) = notifications.findForUser(userId, limit, cursor)
      val unread = notifications.countUnread(userId)
      NotificationsResponse(
        items = items.map { n ->
          NotificationDto(
            id = n.id.toString(),
            body = n.body,
            readAt = n.readAt,
            createdAt = n.createdAt
          )
        },
        unreadCount = unread,
        nextCursor = nextCursor
      )
    }

  suspend fun markRead(
    userId: UUID,
    request: MarkReadRequest
  ): Unit =
    transactor.transaction {
      if (request.all) {
        notifications.markAllRead(userId)
        return@transaction
      }
      if (request.ids.isEmpty()) {
        throw AppException(AppError.ValidationError(mapOf("ids" to "Provide ids or set all=true")))
      }
      val uuids = request.ids.map { parseUuidField("ids", it) }
      notifications.markRead(userId, uuids)
    }
}
