package com.frame.zero.auth

import com.frame.zero.config.dbQuery
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.sql.SQLException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

private const val UNIQUE_VIOLATION_SQL_STATE = "23505"

data class UserRecord(
  val id: UUID,
  val email: String,
  val passwordHash: String,
  val firstName: String,
  val lastName: String,
  val createdAt: Instant
)

interface UserRepository {
  suspend fun findByEmail(email: String): UserRecord?

  suspend fun findById(id: UUID): UserRecord?

  suspend fun create(
    email: String,
    passwordHash: String,
    firstName: String,
    lastName: String
  ): UserRecord
}

class UserRepositoryImpl : UserRepository {
  override suspend fun findByEmail(email: String): UserRecord? =
    dbQuery {
      UsersTable
        .selectAll()
        .where { UsersTable.email eq email.lowercase() }
        .singleOrNull()
        ?.toRecord()
    }

  override suspend fun findById(id: UUID): UserRecord? =
    dbQuery {
      UsersTable
        .selectAll()
        .where { UsersTable.id eq id }
        .singleOrNull()
        ?.toRecord()
    }

  override suspend fun create(
    email: String,
    passwordHash: String,
    firstName: String,
    lastName: String
  ): UserRecord =
    dbQuery {
      val newId = UUID.randomUUID()
      val now = Instant.now().truncatedTo(ChronoUnit.MICROS)
      try {
        UsersTable.insert {
          it[id] = newId
          it[UsersTable.email] = email.lowercase()
          it[UsersTable.passwordHash] = passwordHash
          it[UsersTable.firstName] = firstName
          it[UsersTable.lastName] = lastName
          it[createdAt] = now
        }
      } catch (e: SQLException) {
        // Two concurrent registrations can both pass the service-level
        // findByEmail check; the loser hits the unique index here and must
        // surface as the same 409 as the pre-checked path.
        if (e.isUniqueViolation()) throw AuthException(AuthError.EmailAlreadyExists)
        throw e
      }
      UserRecord(
        id = newId,
        email = email.lowercase(),
        passwordHash = passwordHash,
        firstName = firstName,
        lastName = lastName,
        createdAt = now
      )
    }

  private fun SQLException.isUniqueViolation(): Boolean =
    generateSequence(this as Throwable) { it.cause }
      .filterIsInstance<SQLException>()
      .any { it.sqlState == UNIQUE_VIOLATION_SQL_STATE }

  private fun ResultRow.toRecord(): UserRecord =
    UserRecord(
      id = this[UsersTable.id],
      email = this[UsersTable.email],
      passwordHash = this[UsersTable.passwordHash],
      firstName = this[UsersTable.firstName],
      lastName = this[UsersTable.lastName],
      createdAt = this[UsersTable.createdAt]
    )
}
