package com.frame.zero.notification

import com.frame.zero.dto.device.DevicePlatform
import java.util.UUID

interface DeviceTokenRepository {
  suspend fun upsert(
    userId: UUID,
    token: String,
    platform: DevicePlatform
  )

  suspend fun findTokensForUser(userId: UUID): List<String>

  suspend fun delete(token: String)
}
