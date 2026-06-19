package com.frame.zero.repository.device_token

import com.frame.zero.dto.device.DevicePlatform

interface DeviceTokenRepository {
  suspend fun register(
    token: String,
    platform: DevicePlatform
  )

  suspend fun unregister(token: String)
}
