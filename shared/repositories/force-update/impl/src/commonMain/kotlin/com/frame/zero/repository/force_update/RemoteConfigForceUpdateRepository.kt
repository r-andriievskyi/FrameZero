package com.frame.zero.repository.force_update

import com.frame.zero.dto.device.DevicePlatform
import com.frame.zero.repository.device_token.devicePlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.FirebaseRemoteConfig
import dev.gitlive.firebase.remoteconfig.remoteConfig

class RemoteConfigForceUpdateRepository(
  private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig,
  private val platform: DevicePlatform = devicePlatform()
) : ForceUpdateRepository {
  override suspend fun fetchPolicy(): UpdatePolicy {
    // No in-app defaults: Firebase returns its documented static defaults (0 / "" / false) for any
    // unfetched or unset key, which derive to UpdateType.NONE — so an offline or failed fetch never
    // gates a user. A hard failure here propagates for the check use case to treat as "no update".
    remoteConfig.fetchAndActivate()

    val keys = keysFor(platform)
    return UpdatePolicy(
      minSupportedBuild = remoteConfig.getValue(keys.minSupportedBuild).asLong().toInt(),
      latestBuild = remoteConfig.getValue(keys.latestBuild).asLong().toInt(),
      storeUrl = remoteConfig.getValue(keys.storeUrl).asString(),
      message = remoteConfig.getValue(KEY_MESSAGE).asString().ifBlank { null },
      critical = remoteConfig.getValue(KEY_CRITICAL).asBoolean()
    )
  }
}

internal data class PlatformKeys(
  val minSupportedBuild: String,
  val latestBuild: String,
  val storeUrl: String
)

internal fun keysFor(platform: DevicePlatform): PlatformKeys =
  when (platform) {
    DevicePlatform.ANDROID -> PlatformKeys(
      minSupportedBuild = "min_supported_build_android",
      latestBuild = "latest_build_android",
      storeUrl = "store_url_android"
    )
    DevicePlatform.IOS -> PlatformKeys(
      minSupportedBuild = "min_supported_build_ios",
      latestBuild = "latest_build_ios",
      storeUrl = "store_url_ios"
    )
  }

internal const val KEY_MESSAGE = "update_message"
internal const val KEY_CRITICAL = "update_critical"
