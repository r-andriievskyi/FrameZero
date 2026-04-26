package com.frame.zero.repository

import com.frame.zero.config.dbQuery
import com.frame.zero.database.RefreshTokensTable
import java.time.Instant
import java.util.UUID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

data class RefreshTokenRecord(
  val id: UUID,
  val userId: UUID,
  val tokenHash: String,
  val expiresAt: Instant,
  val revoked: Boolean,
)

interface RefreshTokenRepository {
  suspend fun create(userId: UUID, tokenHash: String, expiresAt: Instant): RefreshTokenRecord

  suspend fun findActiveByHash(tokenHash: String, now: Instant): RefreshTokenRecord?

  suspend fun revoke(tokenHash: String): Boolean
}

class RefreshTokenRepositoryExposed : RefreshTokenRepository {
  override suspend fun create(
    userId: UUID,
    tokenHash: String,
    expiresAt: Instant,
  ): RefreshTokenRecord = dbQuery {
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
      revoked = false,
    )
  }

  override suspend fun findActiveByHash(tokenHash: String, now: Instant): RefreshTokenRecord? =
    dbQuery {
      RefreshTokensTable.selectAll()
        .where {
          (RefreshTokensTable.tokenHash eq tokenHash) and
            (RefreshTokensTable.revoked eq false) and
            (RefreshTokensTable.expiresAt greater now)
        }
        .singleOrNull()
        ?.toRecord()
    }

  override suspend fun revoke(tokenHash: String): Boolean = dbQuery {
    RefreshTokensTable.update({ RefreshTokensTable.tokenHash eq tokenHash }) {
      it[revoked] = true
    } > 0
  }

  private fun ResultRow.toRecord(): RefreshTokenRecord =
    RefreshTokenRecord(
      id = this[RefreshTokensTable.id],
      userId = this[RefreshTokensTable.userId],
      tokenHash = this[RefreshTokensTable.tokenHash],
      expiresAt = this[RefreshTokensTable.expiresAt],
      revoked = this[RefreshTokensTable.revoked],
    )
}
