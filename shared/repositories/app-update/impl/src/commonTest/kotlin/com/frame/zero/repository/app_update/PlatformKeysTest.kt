package com.frame.zero.repository.app_update

import com.frame.zero.dto.device.DevicePlatform
import kotlin.test.Test
import kotlin.test.assertEquals

class PlatformKeysTest {
  @Test
  fun android_platform_selects_android_keys() {
    val keys = keysFor(DevicePlatform.ANDROID)
    assertEquals("min_supported_build_android", keys.minSupportedBuild)
    assertEquals("latest_build_android", keys.latestBuild)
    assertEquals("store_url_android", keys.storeUrl)
  }

  @Test
  fun ios_platform_selects_ios_keys() {
    val keys = keysFor(DevicePlatform.IOS)
    assertEquals("min_supported_build_ios", keys.minSupportedBuild)
    assertEquals("latest_build_ios", keys.latestBuild)
    assertEquals("store_url_ios", keys.storeUrl)
  }

  @Test
  fun platforms_never_share_keys() {
    val android = keysFor(DevicePlatform.ANDROID)
    val ios = keysFor(DevicePlatform.IOS)
    assertEquals(
      emptySet(),
      setOf(android.minSupportedBuild, android.latestBuild, android.storeUrl)
        .intersect(setOf(ios.minSupportedBuild, ios.latestBuild, ios.storeUrl))
    )
  }
}
