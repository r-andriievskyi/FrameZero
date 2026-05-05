package com.frame.zero.core.session

import com.russhwolf.settings.Settings

class TokenStorage(private val settings: Settings) {
  fun saveTokens(
    accessToken: String,
    refreshToken: String
  ) {
    settings.putString(KEY_ACCESS, accessToken)
    settings.putString(KEY_REFRESH, refreshToken)
  }

  fun getAccessToken(): String? = settings.getStringOrNull(KEY_ACCESS)

  fun getRefreshToken(): String? = settings.getStringOrNull(KEY_REFRESH)

  fun hasTokens(): Boolean = getAccessToken() != null && getRefreshToken() != null

  fun clearTokens() {
    settings.remove(KEY_ACCESS)
    settings.remove(KEY_REFRESH)
  }

  private companion object {
    const val KEY_ACCESS = "auth.access_token"
    const val KEY_REFRESH = "auth.refresh_token"
  }
}
