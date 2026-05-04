package com.frame.zero.database

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.timestamp

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

object RefreshTokensTable : Table("refresh_tokens") {
  val id = javaUUID("id")
  val userId = javaUUID("user_id").references(UsersTable.id)
  val tokenHash = varchar("token_hash", length = HASH_MAX).uniqueIndex()
  val expiresAt = timestamp("expires_at")
  val revoked = bool("revoked").default(false)
  val createdAt = timestamp("created_at")

  override val primaryKey = PrimaryKey(id)

  private const val HASH_MAX = 64
}
