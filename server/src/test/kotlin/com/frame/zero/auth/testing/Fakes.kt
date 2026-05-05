package com.frame.zero.auth.testing

import com.frame.zero.repository.RefreshTokenRecord
import com.frame.zero.repository.RefreshTokenRepository
import com.frame.zero.repository.UserRecord
import com.frame.zero.repository.UserRepository
import java.time.Instant
import java.util.UUID

internal class FakeUserRepository : UserRepository {
  val users: MutableList<UserRecord> = mutableListOf()

  override suspend fun findByEmail(email: String): UserRecord? =
    users.firstOrNull {
      it.email == email.lowercase()
    }

  override suspend fun findById(id: UUID): UserRecord? = users.firstOrNull { it.id == id }

  override suspend fun create(
    email: String,
    passwordHash: String,
    firstName: String,
    lastName: String,
  ): UserRecord {
    val record =
      UserRecord(
        id = UUID.randomUUID(),
        email = email.lowercase(),
        passwordHash = passwordHash,
        firstName = firstName,
        lastName = lastName,
        createdAt = Instant.now(),
      )
    users += record
    return record
  }

  fun deleteAll() {
    users.clear()
  }
}

internal class FakeRefreshTokenRepository : RefreshTokenRepository {
  val records: MutableList<RefreshTokenRecord> = mutableListOf()

  override suspend fun create(
    userId: UUID,
    tokenHash: String,
    expiresAt: Instant,
  ): RefreshTokenRecord {
    val record =
      RefreshTokenRecord(
        id = UUID.randomUUID(),
        userId = userId,
        tokenHash = tokenHash,
        expiresAt = expiresAt,
        revoked = false,
      )
    records += record
    return record
  }

  override suspend fun findActiveByHash(
    tokenHash: String,
    now: Instant
  ): RefreshTokenRecord? =
    records.firstOrNull {
      it.tokenHash == tokenHash && !it.revoked && it.expiresAt.isAfter(now)
    }

  override suspend fun revoke(tokenHash: String): Boolean {
    val idx = records.indexOfFirst { it.tokenHash == tokenHash }
    if (idx < 0) return false
    records[idx] = records[idx].copy(revoked = true)
    return true
  }
}
