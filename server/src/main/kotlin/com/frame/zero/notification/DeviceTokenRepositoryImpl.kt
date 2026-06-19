package com.frame.zero.notification

import com.frame.zero.common.nowTruncatedToMicros
import com.frame.zero.config.dbQuery
import com.frame.zero.dto.device.DevicePlatform
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import java.util.UUID

class DeviceTokenRepositoryImpl : DeviceTokenRepository {
  override suspend fun upsert(
    userId: UUID,
    token: String,
    platform: DevicePlatform
  ): Unit =
    dbQuery {
      val now = nowTruncatedToMicros()
      DeviceTokensTable.upsert(
        DeviceTokensTable.token,
        onUpdateExclude = listOf(DeviceTokensTable.id, DeviceTokensTable.createdAt)
      ) {
        it[id] = UUID.randomUUID()
        it[DeviceTokensTable.userId] = userId
        it[DeviceTokensTable.token] = token
        it[DeviceTokensTable.platform] = platform.name
        it[createdAt] = now
        it[updatedAt] = now
      }
    }

  override suspend fun findTokensForUser(userId: UUID): List<String> =
    dbQuery {
      DeviceTokensTable
        .selectAll()
        .where { DeviceTokensTable.userId eq userId }
        .map { it[DeviceTokensTable.token] }
    }

  override suspend fun delete(token: String): Unit =
    dbQuery {
      DeviceTokensTable.deleteWhere { DeviceTokensTable.token eq token }
    }
}

