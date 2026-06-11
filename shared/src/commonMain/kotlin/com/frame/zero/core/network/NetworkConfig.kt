package com.frame.zero.core.network

import com.frame.zero.SERVER_PORT

data class NetworkConfig(
  val baseUrl: String
) {
  companion object {
    /**
     * The build-time base URL when one is configured (release builds, via
     * BuildKonfig), otherwise the platform-specific localhost dev server.
     */
    fun fromBuildConfig(): NetworkConfig =
      if (BuildKonfig.BASE_URL.isNotEmpty()) {
        NetworkConfig(baseUrl = BuildKonfig.BASE_URL)
      } else {
        NetworkConfig(baseUrl = "http://${localhostHost()}:$SERVER_PORT")
      }
  }
}

internal expect fun localhostHost(): String
