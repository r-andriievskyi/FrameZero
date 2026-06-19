package com.frame.zero.dto.device

import kotlinx.serialization.Serializable

@Serializable
enum class DevicePlatform {
  ANDROID,
  IOS
}

@Serializable
data class RegisterDeviceTokenRequest(
  val token: String,
  val platform: DevicePlatform
)

@Serializable
data class UnregisterDeviceTokenRequest(
  val token: String
)
