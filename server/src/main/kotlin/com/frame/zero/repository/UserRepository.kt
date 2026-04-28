package com.frame.zero.repository

import com.frame.zero.config.dbQuery
import com.frame.zero.database.UsersTable
import java.time.Instant
import java.util.UUID
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll

data class UserRecord(
  val id: UUID,
  val email: String,
  val passwordHash: String,
  val createdAt: Instant,
)

interface UserRepository {
  suspend fun findByEmail(email: String): UserRecord?

  suspend fun findById(id: UUID): UserRecord?

  suspend fun create(email: String, passwordHash: String): UserRecord
}

class UserRepositoryExposed : UserRepository {
  override suspend fun findByEmail(email: String): UserRecord? = dbQuery {
    UsersTable.selectAll()
      .where { UsersTable.email eq email.lowercase() }
      .singleOrNull()
      ?.toRecord()
  }

  override suspend fun findById(id: UUID): UserRecord? = dbQuery {
    UsersTable.selectAll().where { UsersTable.id eq id }.singleOrNull()?.toRecord()
  }

  override suspend fun create(email: String, passwordHash: String): UserRecord = dbQuery {
    val newId = UUID.randomUUID()
    val now = Instant.now()
    UsersTable.insert {
      it[id] = newId
      it[UsersTable.email] = email.lowercase()
      it[UsersTable.passwordHash] = passwordHash
      it[createdAt] = now
    }
    UserRecord(id = newId, email = email.lowercase(), passwordHash = passwordHash, createdAt = now)
  }

  private fun ResultRow.toRecord(): UserRecord =
    UserRecord(
      id = this[UsersTable.id],
      email = this[UsersTable.email],
      passwordHash = this[UsersTable.passwordHash],
      createdAt = this[UsersTable.createdAt],
    )
}
