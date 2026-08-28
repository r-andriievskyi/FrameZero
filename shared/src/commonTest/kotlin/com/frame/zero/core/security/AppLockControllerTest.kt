package com.frame.zero.core.security

import app.cash.turbine.test
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppLockControllerTest {
  private val prompt = BiometricPromptText(title = "t", subtitle = "s", negativeButton = "c")

  @Test
  fun `starts unlocked when the feature is disabled`() =
    runTest {
      val manager = AppLockController(FakeAuthenticator(), MapSettings())

      manager.lockState.test {
        assertEquals(AppLockState.Unlocked, awaitItem())
      }
      assertFalse(manager.isEnabled)
    }

  @Test
  fun `starts locked when the feature was previously enabled`() =
    runTest {
      val settings = MapSettings().apply { putBoolean("security.app_lock_enabled", true) }
      val manager = AppLockController(FakeAuthenticator(), settings)

      manager.lockState.test {
        assertEquals(AppLockState.Locked, awaitItem())
      }
      assertTrue(manager.isEnabled)
    }

  @Test
  fun `backgrounding re-locks only when enabled`() =
    runTest {
      val disabled = AppLockController(FakeAuthenticator(), MapSettings())
      disabled.lockState.test {
        disabled.onBackgrounded()
        advanceUntilIdle()
        assertEquals(AppLockState.Unlocked, expectMostRecentItem())
      }

      val settings = MapSettings().apply { putBoolean("security.app_lock_enabled", true) }
      val enabled = AppLockController(FakeAuthenticator(), settings)
      enabled.lockState.test {
        enabled.setEnabled(true) // unlocks within the session
        advanceUntilIdle()
        assertEquals(AppLockState.Unlocked, expectMostRecentItem())
        enabled.onBackgrounded()
        advanceUntilIdle()
        assertEquals(AppLockState.Locked, expectMostRecentItem())
      }
    }

  @Test
  fun `a successful prompt unlocks`() =
    runTest {
      val settings = MapSettings().apply { putBoolean("security.app_lock_enabled", true) }
      val manager = AppLockController(FakeAuthenticator(BiometricResult.Success), settings)

      manager.lockState.test {
        assertEquals(AppLockState.Locked, awaitItem())

        val result = manager.authenticate(prompt)
        assertEquals(BiometricResult.Success, result)

        advanceUntilIdle()
        assertEquals(AppLockState.Unlocked, expectMostRecentItem())
      }
    }

  @Test
  fun `a failed prompt stays locked`() =
    runTest {
      val settings = MapSettings().apply { putBoolean("security.app_lock_enabled", true) }
      val manager = AppLockController(FakeAuthenticator(BiometricResult.Cancelled), settings)

      manager.lockState.test {
        manager.authenticate(prompt)
        advanceUntilIdle()
        assertEquals(AppLockState.Locked, expectMostRecentItem())
      }
    }

  @Test
  fun `setEnabled drives the enabled flow`() =
    runTest {
      val manager = AppLockController(FakeAuthenticator(), MapSettings())

      manager.enabled.test {
        assertFalse(awaitItem())

        manager.setEnabled(true)
        advanceUntilIdle()
        assertTrue(expectMostRecentItem())
      }
      assertTrue(manager.isEnabled)
    }

  @Test
  fun `rejects a second prompt while one is in flight`() =
    runTest {
      val gate = CompletableDeferred<Unit>()
      val settings = MapSettings().apply { putBoolean("security.app_lock_enabled", true) }
      val manager = AppLockController(BlockingAuthenticator(gate), settings)

      manager.lockState.test {
        val first = launch { manager.authenticate(prompt) }
        runCurrent() // first call acquires the gate, then suspends inside the authenticator

        val second = manager.authenticate(prompt)
        assertEquals(BiometricResult.Cancelled, second)

        gate.complete(Unit)
        first.join()
        advanceUntilIdle()
        assertEquals(AppLockState.Unlocked, expectMostRecentItem())
      }
    }

  private class FakeAuthenticator(
    private val result: BiometricResult = BiometricResult.Success
  ) : BiometricAuthenticator {
    override fun availability(): BiometricAvailability = BiometricAvailability.Available

    override suspend fun authenticate(prompt: BiometricPromptText): BiometricResult = result
  }

  // Suspends inside authenticate() until [gate] completes, so a second concurrent call can
  // be observed hitting the in-flight guard.
  private class BlockingAuthenticator(
    private val gate: CompletableDeferred<Unit>
  ) : BiometricAuthenticator {
    override fun availability(): BiometricAvailability = BiometricAvailability.Available

    override suspend fun authenticate(prompt: BiometricPromptText): BiometricResult {
      gate.await()
      return BiometricResult.Success
    }
  }
}
