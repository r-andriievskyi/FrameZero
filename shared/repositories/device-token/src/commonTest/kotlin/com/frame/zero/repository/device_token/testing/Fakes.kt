package com.frame.zero.repository.device_token.testing

import com.frame.zero.core.push.PushTokenProvider
import com.frame.zero.dto.device.DevicePlatform
import com.frame.zero.repository.device_token.DeviceTokenRepository

internal class FakeDeviceTokenRepository : DeviceTokenRepository {
  data class Registration(
    val token: String,
    val platform: DevicePlatform
  )

  val registered: MutableList<Registration> = mutableListOf()
  val unregistered: MutableList<String> = mutableListOf()

  override suspend fun register(
    token: String,
    platform: DevicePlatform
  ) {
    registered += Registration(token, platform)
  }

  override suspend fun unregister(token: String) {
    unregistered += token
  }
}

internal class FakePushTokenProvider(
  var token: String? = "fcm-token"
) : PushTokenProvider {
  override suspend fun currentToken(): String? = token
}
