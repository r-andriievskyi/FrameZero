package com.frame.zero.notification

import com.frame.zero.auth.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

object DeviceTokensTable : Table("device_tokens") {
  val id = javaUUID("id")
  val userId = javaUUID("user_id").references(UsersTable.id)
  val token = varchar("token", 512).uniqueIndex()
  val platform = varchar("platform", 10)
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")

  override val primaryKey = PrimaryKey(id)

  init {
    index("idx_device_tokens_user", false, userId)
  }
}
