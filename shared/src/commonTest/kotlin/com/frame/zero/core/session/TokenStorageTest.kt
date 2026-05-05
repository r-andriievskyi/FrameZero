package com.frame.zero.core.session

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TokenStorageTest {
  @Test
  fun `getAccessToken returns null when nothing stored`() {
    val storage = TokenStorage(MapSettings())

    assertNull(storage.getAccessToken())
  }

  @Test
  fun `getRefreshToken returns null when nothing stored`() {
    val storage = TokenStorage(MapSettings())

    assertNull(storage.getRefreshToken())
  }

  @Test
  fun `saveTokens persists access token`() {
    val storage = TokenStorage(MapSettings())

    storage.saveTokens(accessToken = "a-1", refreshToken = "r-1")

    assertEquals("a-1", storage.getAccessToken())
  }

  @Test
  fun `saveTokens persists refresh token`() {
    val storage = TokenStorage(MapSettings())

    storage.saveTokens(accessToken = "a-1", refreshToken = "r-1")

    assertEquals("r-1", storage.getRefreshToken())
  }

  @Test
  fun `saveTokens overwrites previous values`() {
    val storage = TokenStorage(MapSettings())
    storage.saveTokens("a-old", "r-old")

    storage.saveTokens("a-new", "r-new")

    assertEquals("a-new", storage.getAccessToken())
    assertEquals("r-new", storage.getRefreshToken())
  }

  @Test
  fun `hasTokens is false when nothing stored`() {
    val storage = TokenStorage(MapSettings())

    assertFalse(storage.hasTokens())
  }

  @Test
  fun `hasTokens is false when only access token is stored`() {
    val settings = MapSettings()
    settings.putString("auth.access_token", "a-1")
    val storage = TokenStorage(settings)

    assertFalse(storage.hasTokens())
  }

  @Test
  fun `hasTokens is false when only refresh token is stored`() {
    val settings = MapSettings()
    settings.putString("auth.refresh_token", "r-1")
    val storage = TokenStorage(settings)

    assertFalse(storage.hasTokens())
  }

  @Test
  fun `hasTokens is true when both tokens are stored`() {
    val storage = TokenStorage(MapSettings())
    storage.saveTokens("a-1", "r-1")

    assertTrue(storage.hasTokens())
  }

  @Test
  fun `clearTokens removes both tokens`() {
    val storage = TokenStorage(MapSettings())
    storage.saveTokens("a-1", "r-1")

    storage.clearTokens()

    assertNull(storage.getAccessToken())
    assertNull(storage.getRefreshToken())
    assertFalse(storage.hasTokens())
  }

  @Test
  fun `clearTokens on empty storage is a no-op`() {
    val storage = TokenStorage(MapSettings())

    storage.clearTokens()

    assertFalse(storage.hasTokens())
  }
}
