package com.frame.zero.notification

import com.frame.zero.common.decodeCursor
import com.frame.zero.common.encodeCursor
import com.frame.zero.config.dbQuery
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import java.util.UUID

data class NotificationRecord(
  val id: UUID,
  val userId: UUID,
  val title: String,
  val body: String?,
  val readAt: Instant?,
  val createdAt: Instant
)

interface NotificationRepository {
  suspend fun findForUser(
    userId: UUID,
    limit: Int,
    cursor: String?
  ): Pair<List<NotificationRecord>, String?>

  suspend fun countUnread(userId: UUID): Int

  suspend fun markRead(
    userId: UUID,
    ids: List<UUID>
  )

  suspend fun markAllRead(userId: UUID)

  suspend fun create(
    userId: UUID,
    title: String,
    body: String?
  ): NotificationRecord
}

class NotificationRepositoryImpl : NotificationRepository {
  override suspend fun findForUser(
    userId: UUID,
    limit: Int,
    cursor: String?
  ): Pair<List<NotificationRecord>, String?> =
    dbQuery {
      val rows = NotificationsTable
        .selectAll()
        .where {
          var cond = NotificationsTable.userId eq userId
          if (cursor != null) {
            val pc = decodeCursor(cursor)
            if (pc != null) {
              val cursorTs = Instant.ofEpochMilli(pc.epochMillis)
              cond =
                cond and
                (
                  (NotificationsTable.createdAt less cursorTs) or
                    (
                      (NotificationsTable.createdAt eq cursorTs) and
                        (NotificationsTable.id less pc.id)
                    )
                )
            }
          }
          cond
        }.orderBy(
          NotificationsTable.createdAt to SortOrder.DESC,
          NotificationsTable.id to SortOrder.DESC
        ).limit(limit + 1)
        .map { it.toRecord() }

      val hasMore = rows.size > limit
      val items = if (hasMore) rows.dropLast(1) else rows
      val nextCursor =
        if (hasMore) {
          val last = items.last()
          encodeCursor(last.createdAt.toEpochMilli(), last.id)
        } else {
          null
        }
      Pair(items, nextCursor)
    }

  override suspend fun countUnread(userId: UUID): Int =
    dbQuery {
      NotificationsTable
        .selectAll()
        .where { (NotificationsTable.userId eq userId) and NotificationsTable.readAt.isNull() }
        .count()
        .toInt()
    }

  override suspend fun markRead(
    userId: UUID,
    ids: List<UUID>
  ): Unit =
    dbQuery {
      if (ids.isEmpty()) return@dbQuery
      val now = Instant.now()
      NotificationsTable.update({
        (NotificationsTable.userId eq userId) and
          (NotificationsTable.id inList ids) and
          NotificationsTable.readAt.isNull()
      }) {
        it[readAt] = now
      }
    }

  override suspend fun markAllRead(userId: UUID): Unit =
    dbQuery {
      val now = Instant.now()
      NotificationsTable.update({
        (NotificationsTable.userId eq userId) and NotificationsTable.readAt.isNull()
      }) {
        it[readAt] = now
      }
    }

  override suspend fun create(
    userId: UUID,
    title: String,
    body: String?
  ): NotificationRecord =
    dbQuery {
      val newId = UUID.randomUUID()
      val now = Instant.now()
      NotificationsTable.insert {
        it[id] = newId
        it[NotificationsTable.userId] = userId
        it[NotificationsTable.title] = title
        it[NotificationsTable.body] = body
        it[createdAt] = now
      }
      NotificationRecord(
        id = newId,
        userId = userId,
        title = title,
        body = body,
        readAt = null,
        createdAt = now
      )
    }

  private fun ResultRow.toRecord(): NotificationRecord =
    NotificationRecord(
      id = this[NotificationsTable.id],
      userId = this[NotificationsTable.userId],
      title = this[NotificationsTable.title],
      body = this[NotificationsTable.body],
      readAt = this[NotificationsTable.readAt],
      createdAt = this[NotificationsTable.createdAt]
    )
}
