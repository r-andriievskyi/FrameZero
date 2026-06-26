package com.frame.zero.repository.device_token

import com.frame.zero.core.session.TokenStorage
import com.frame.zero.repository.device_token.testing.FakeDeviceTokenRepository
import com.frame.zero.repository.device_token.testing.FakePushTokenProvider
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeviceTokenSessionCleanerTest {
  private fun tokenStorage(hasTokens: Boolean) =
    TokenStorage(MapSettings()).also { if (hasTokens) it.saveTokens("a", "r") }

  @Test
  fun `clear unregisters the current token`() =
    runTest {
      val repo = FakeDeviceTokenRepository()
      DeviceTokenSessionCleaner(FakePushTokenProvider("tok-1"), repo, tokenStorage(hasTokens = true)).clear()

      assertEquals(listOf("tok-1"), repo.unregistered)
    }

  @Test
  fun `clear with no current token is a no-op`() =
    runTest {
      val repo = FakeDeviceTokenRepository()
      DeviceTokenSessionCleaner(FakePushTokenProvider(token = null), repo, tokenStorage(hasTokens = true)).clear()

      assertTrue(repo.unregistered.isEmpty())
    }

  @Test
  fun `clear is a no-op when the session is already unauthenticated`() =
    runTest {
      val repo = FakeDeviceTokenRepository()
      DeviceTokenSessionCleaner(FakePushTokenProvider("tok-1"), repo, tokenStorage(hasTokens = false)).clear()

      assertTrue(repo.unregistered.isEmpty())
    }
}
