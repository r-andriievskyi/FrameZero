package com.frame.zero.notification.testing

import com.frame.zero.notification.NotificationRecord
import com.frame.zero.notification.NotificationRepository
import kotlin.time.Clock
import java.util.UUID

internal class FakeNotificationRepository : NotificationRepository {
  val notifications: MutableList<NotificationRecord> = mutableListOf()

  override suspend fun findForUser(
    userId: UUID,
    limit: Int,
    cursor: String?
  ): Pair<List<NotificationRecord>, String?> {
    val items = notifications.filter { it.userId == userId }.take(limit)
    return Pair(items, null)
  }

  override suspend fun countUnread(userId: UUID): Int =
    notifications.count {
      it.userId == userId && it.readAt == null
    }

  override suspend fun markRead(
    userId: UUID,
    ids: List<UUID>
  ) {
    val now = Clock.System.now()
    ids.forEach { id ->
      val idx = notifications.indexOfFirst { it.id == id && it.userId == userId }
      if (idx >= 0) notifications[idx] = notifications[idx].copy(readAt = now)
    }
  }

  override suspend fun markAllRead(userId: UUID) {
    val now = Clock.System.now()
    notifications.replaceAll { n ->
      if (n.userId == userId && n.readAt == null) n.copy(readAt = now) else n
    }
  }

  override suspend fun create(
    userId: UUID,
    body: String?
  ): NotificationRecord {
    val record =
      NotificationRecord(
        id = UUID.randomUUID(),
        userId = userId,
        body = body,
        readAt = null,
        createdAt = Clock.System.now()
      )
    notifications += record
    return record
  }
}
