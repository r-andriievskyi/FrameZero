package com.frame.zero.repository.device_token

import com.frame.zero.core.push.PushTokenProvider
import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.core.session.TokenStorage

internal class DeviceTokenSessionCleaner(
  private val tokenProvider: PushTokenProvider,
  private val repository: DeviceTokenRepository,
  private val tokenStorage: TokenStorage
) : SessionCleaner {
  override suspend fun clear() {
    if (!tokenStorage.hasTokens()) return
    val token = tokenProvider.currentToken() ?: return
    repository.unregister(token)
  }
}
