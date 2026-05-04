package com.frame.zero.database

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.timestamp

object NotificationsTable : Table("notifications") {
  val id = javaUUID("id")
  val userId = javaUUID("user_id").references(UsersTable.id)
  val title = varchar("title", 200)
  val body = text("body").nullable()
  val readAt = timestamp("read_at").nullable()
  val createdAt = timestamp("created_at")

  override val primaryKey = PrimaryKey(id)
}
