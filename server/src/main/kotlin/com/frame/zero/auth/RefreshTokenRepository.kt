package com.frame.zero.auth

import com.frame.zero.config.dbQuery
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import java.util.UUID

data class RefreshTokenRecord(
  val id: UUID,
  val userId: UUID,
  val tokenHash: String,
  val expiresAt: Instant,
  val revoked: Boolean
)

interface RefreshTokenRepository {
  suspend fun create(
    userId: UUID,
    tokenHash: String,
    expiresAt: Instant
  ): RefreshTokenRecord

  suspend fun findByHash(tokenHash: String): RefreshTokenRecord?

  /**
   * Atomically revokes the token if it is still active. Returns the claimed
   * record, or null when the token is unknown, expired, or already revoked —
   * concurrent claims of the same token succeed at most once.
   */
  suspend fun claim(
    tokenHash: String,
    now: Instant
  ): RefreshTokenRecord?

  suspend fun revoke(tokenHash: String): Boolean

  suspend fun revokeAllForUser(userId: UUID): Int
}

class RefreshTokenRepositoryImpl : RefreshTokenRepository {
  override suspend fun create(
    userId: UUID,
    tokenHash: String,
    expiresAt: Instant
  ): RefreshTokenRecord =
    dbQuery {
      val newId = UUID.randomUUID()
      val now = Instant.now()
      RefreshTokensTable.insert {
        it[id] = newId
        it[RefreshTokensTable.userId] = userId
        it[RefreshTokensTable.tokenHash] = tokenHash
        it[RefreshTokensTable.expiresAt] = expiresAt
        it[revoked] = false
        it[createdAt] = now
      }
      RefreshTokenRecord(
        id = newId,
        userId = userId,
        tokenHash = tokenHash,
        expiresAt = expiresAt,
        revoked = false
      )
    }

  override suspend fun findByHash(tokenHash: String): RefreshTokenRecord? =
    dbQuery {
      RefreshTokensTable
        .selectAll()
        .where { RefreshTokensTable.tokenHash eq tokenHash }
        .singleOrNull()
        ?.toRecord()
    }

  override suspend fun claim(
    tokenHash: String,
    now: Instant
  ): RefreshTokenRecord? =
    dbQuery {
      val claimed = RefreshTokensTable.update({
        (RefreshTokensTable.tokenHash eq tokenHash) and
          (RefreshTokensTable.revoked eq false) and
          (RefreshTokensTable.expiresAt greater now)
      }) {
        it[revoked] = true
      } > 0
      if (claimed) {
        RefreshTokensTable
          .selectAll()
          .where { RefreshTokensTable.tokenHash eq tokenHash }
          .singleOrNull()
          ?.toRecord()
      } else {
        null
      }
    }

  override suspend fun revoke(tokenHash: String): Boolean =
    dbQuery {
      RefreshTokensTable.update({ RefreshTokensTable.tokenHash eq tokenHash }) {
        it[revoked] = true
      } > 0
    }

  override suspend fun revokeAllForUser(userId: UUID): Int =
    dbQuery {
      RefreshTokensTable.update({
        (RefreshTokensTable.userId eq userId) and (RefreshTokensTable.revoked eq false)
      }) {
        it[revoked] = true
      }
    }

  private fun ResultRow.toRecord(): RefreshTokenRecord =
    RefreshTokenRecord(
      id = this[RefreshTokensTable.id],
      userId = this[RefreshTokensTable.userId],
      tokenHash = this[RefreshTokensTable.tokenHash],
      expiresAt = this[RefreshTokensTable.expiresAt],
      revoked = this[RefreshTokensTable.revoked]
    )
}
