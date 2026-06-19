package com.frame.zero.repository.device_token

import com.frame.zero.core.push.PushTokenProvider
import com.frame.zero.core.session.SessionCleaner

internal class DeviceTokenSessionCleaner(
  private val tokenProvider: PushTokenProvider,
  private val repository: DeviceTokenRepository
) : SessionCleaner {
  override suspend fun clear() {
    val token = tokenProvider.currentToken() ?: return
    repository.unregister(token)
  }
}
