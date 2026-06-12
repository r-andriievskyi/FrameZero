package com.frame.zero.auth

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

object UsersTable : Table("users") {
  val id = javaUUID("id")
  val email = varchar("email", length = EMAIL_MAX).uniqueIndex()
  val passwordHash = varchar("password_hash", length = HASH_MAX)
  val firstName = varchar("first_name", length = NAME_MAX)
  val lastName = varchar("last_name", length = NAME_MAX)
  val avatarColorHex = varchar("avatar_color_hex", length = 7).nullable()
  val createdAt = timestamp("created_at")

  override val primaryKey = PrimaryKey(id)

  private const val EMAIL_MAX = 320
  private const val HASH_MAX = 100
  private const val NAME_MAX = 100
}
