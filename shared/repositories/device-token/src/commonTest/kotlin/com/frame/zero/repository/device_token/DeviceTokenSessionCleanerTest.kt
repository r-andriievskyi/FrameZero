package com.frame.zero.repository.device_token

import com.frame.zero.repository.device_token.testing.FakeDeviceTokenRepository
import com.frame.zero.repository.device_token.testing.FakePushTokenProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeviceTokenSessionCleanerTest {
  @Test
  fun `clear unregisters the current token`() =
    runTest {
      val repo = FakeDeviceTokenRepository()
      DeviceTokenSessionCleaner(FakePushTokenProvider("tok-1"), repo).clear()

      assertEquals(listOf("tok-1"), repo.unregistered)
    }

  @Test
  fun `clear with no current token is a no-op`() =
    runTest {
      val repo = FakeDeviceTokenRepository()
      DeviceTokenSessionCleaner(FakePushTokenProvider(token = null), repo).clear()

      assertTrue(repo.unregistered.isEmpty())
    }
}
