package com.frame.zero.auth.testing

import com.frame.zero.auth.RefreshTokenRecord
import com.frame.zero.auth.RefreshTokenRepository
import com.frame.zero.auth.UserRecord
import com.frame.zero.auth.UserRepository
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
    lastName: String
  ): UserRecord {
    val record =
      UserRecord(
        id = UUID.randomUUID(),
        email = email.lowercase(),
        passwordHash = passwordHash,
        firstName = firstName,
        lastName = lastName,
        createdAt = Instant.now()
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
    expiresAt: Instant
  ): RefreshTokenRecord {
    val record =
      RefreshTokenRecord(
        id = UUID.randomUUID(),
        userId = userId,
        tokenHash = tokenHash,
        expiresAt = expiresAt,
        revoked = false
      )
    records += record
    return record
  }

  override suspend fun findByHash(tokenHash: String): RefreshTokenRecord? =
    records.firstOrNull { it.tokenHash == tokenHash }

  override suspend fun claim(
    tokenHash: String,
    now: Instant
  ): RefreshTokenRecord? {
    val idx = records.indexOfFirst {
      it.tokenHash == tokenHash && !it.revoked && it.expiresAt.isAfter(now)
    }
    if (idx < 0) return null
    records[idx] = records[idx].copy(revoked = true)
    return records[idx]
  }

  override suspend fun revoke(tokenHash: String): Boolean {
    val idx = records.indexOfFirst { it.tokenHash == tokenHash }
    if (idx < 0) return false
    records[idx] = records[idx].copy(revoked = true)
    return true
  }

  override suspend fun revokeAllForUser(userId: UUID): Int {
    var count = 0
    records.replaceAll { record ->
      if (record.userId == userId && !record.revoked) {
        count++
        record.copy(revoked = true)
      } else {
        record
      }
    }
    return count
  }
}
