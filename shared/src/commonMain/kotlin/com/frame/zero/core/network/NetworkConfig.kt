package com.frame.zero.core.network

import com.frame.zero.SERVER_PORT

data class NetworkConfig(
  val baseUrl: String,
  val isDebug: Boolean
) {
  companion object {
    /**
     * The build-time base URL when one is configured (release builds, via
     * BuildKonfig), otherwise the platform-specific localhost dev server.
     * [isDebug] mirrors the build flavor so consumers outside `shared` (which can't
     * read the internal generated `BuildKonfig`) can gate debug-only behavior.
     */
    fun fromBuildConfig(): NetworkConfig =
      if (BuildKonfig.BASE_URL.isNotEmpty()) {
        NetworkConfig(baseUrl = BuildKonfig.BASE_URL, isDebug = BuildKonfig.DEBUG)
      } else {
        NetworkConfig(baseUrl = "http://${localhostHost()}:$SERVER_PORT", isDebug = BuildKonfig.DEBUG)
      }
  }
}

internal expect fun localhostHost(): String
