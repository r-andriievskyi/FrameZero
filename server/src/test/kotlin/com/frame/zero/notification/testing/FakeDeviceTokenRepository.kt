package com.frame.zero.notification.testing

import com.frame.zero.dto.device.DevicePlatform
import com.frame.zero.notification.DeviceTokenRepository
import java.util.UUID

internal class FakeDeviceTokenRepository : DeviceTokenRepository {
  data class Entry(
    val userId: UUID,
    val token: String,
    val platform: DevicePlatform
  )

  val entries: MutableList<Entry> = mutableListOf()

  override suspend fun upsert(
    userId: UUID,
    token: String,
    platform: DevicePlatform
  ) {
    entries.removeAll { it.token == token }
    entries += Entry(userId, token, platform)
  }

  override suspend fun findTokensForUser(userId: UUID): List<String> =
    entries.filter { it.userId == userId }.map { it.token }

  override suspend fun delete(token: String) {
    entries.removeAll { it.token == token }
  }
}
