package com.frame.zero.notification

import com.frame.zero.common.Transactor
import com.frame.zero.dto.device.RegisterDeviceTokenRequest
import com.frame.zero.dto.device.UnregisterDeviceTokenRequest
import java.util.UUID

class DeviceTokenService(
  private val deviceTokens: DeviceTokenRepository,
  private val transactor: Transactor
) {
  suspend fun register(
    userId: UUID,
    request: RegisterDeviceTokenRequest
  ): Unit = transactor.transaction {
    deviceTokens.upsert(userId, request.token, request.platform)
  }

  suspend fun unregister(
    @Suppress("UNUSED_PARAMETER") userId: UUID,
    request: UnregisterDeviceTokenRequest
  ): Unit = transactor.transaction {
      // The token is unique and is the natural key; userId is accepted for symmetry
      // with register and to keep the endpoint authenticated, but deletion is by token.
      deviceTokens.delete(request.token)
    }
}
