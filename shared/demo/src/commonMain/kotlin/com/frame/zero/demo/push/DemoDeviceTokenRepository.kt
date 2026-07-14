package com.frame.zero.demo.push

import com.frame.zero.dto.device.DevicePlatform
import com.frame.zero.repository.device_token.DeviceTokenRepository

internal class DemoDeviceTokenRepository : DeviceTokenRepository {
  override suspend fun register(
    token: String,
    platform: DevicePlatform
  ) = Unit

  override suspend fun unregister(token: String) = Unit
}
