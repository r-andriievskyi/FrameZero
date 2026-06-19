package com.frame.zero.repository.device_token

import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.dto.device.DevicePlatform
import com.frame.zero.dto.device.RegisterDeviceTokenRequest
import com.frame.zero.dto.device.UnregisterDeviceTokenRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class DeviceTokenRepositoryImpl(
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig
) : DeviceTokenRepository {
  override suspend fun register(
    token: String,
    platform: DevicePlatform
  ) {
    httpClient.post("${networkConfig.baseUrl}/api/v1/device-tokens") {
      setBody(RegisterDeviceTokenRequest(token = token, platform = platform))
    }
  }

  override suspend fun unregister(token: String) {
    httpClient.delete("${networkConfig.baseUrl}/api/v1/device-tokens") {
      setBody(UnregisterDeviceTokenRequest(token = token))
    }
  }
}

