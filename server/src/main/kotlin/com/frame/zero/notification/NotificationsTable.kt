package com.frame.zero.notification

import com.frame.zero.auth.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

object NotificationsTable : Table("notifications") {
  val id = javaUUID("id")
  val userId = javaUUID("user_id").references(UsersTable.id)
  val body = text("body").nullable()
  val readAt = timestamp("read_at").nullable()
  val createdAt = timestamp("created_at")

  override val primaryKey = PrimaryKey(id)

  init {
    index("idx_notifications_user", false, userId, createdAt)
  }
}
