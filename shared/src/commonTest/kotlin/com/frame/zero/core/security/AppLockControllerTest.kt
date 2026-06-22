package com.frame.zero.core.security

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppLockControllerTest {
  private val prompt = BiometricPromptText(title = "t", subtitle = "s", negativeButton = "c")

  @Test
  fun `starts unlocked when the feature is disabled`() {
    val manager = AppLockController(FakeAuthenticator(), MapSettings())

    assertEquals(AppLockState.Unlocked, manager.lockState.value)
    assertFalse(manager.isEnabled)
  }

  @Test
  fun `starts locked when the feature was previously enabled`() {
    val settings = MapSettings().apply { putBoolean("security.app_lock_enabled", true) }
    val manager = AppLockController(FakeAuthenticator(), settings)

    assertEquals(AppLockState.Locked, manager.lockState.value)
    assertTrue(manager.isEnabled)
  }

  @Test
  fun `backgrounding re-locks only when enabled`() {
    val disabled = AppLockController(FakeAuthenticator(), MapSettings())
    disabled.onBackgrounded()
    assertEquals(AppLockState.Unlocked, disabled.lockState.value)

    val settings = MapSettings().apply { putBoolean("security.app_lock_enabled", true) }
    val enabled = AppLockController(FakeAuthenticator(), settings)
    enabled.setEnabled(true) // unlocks within the session
    assertEquals(AppLockState.Unlocked, enabled.lockState.value)
    enabled.onBackgrounded()
    assertEquals(AppLockState.Locked, enabled.lockState.value)
  }

  @Test
  fun `a successful prompt unlocks`() =
    runTest {
      val settings = MapSettings().apply { putBoolean("security.app_lock_enabled", true) }
      val manager = AppLockController(FakeAuthenticator(BiometricResult.Success), settings)
      assertEquals(AppLockState.Locked, manager.lockState.value)

      val result = manager.authenticate(prompt)

      assertEquals(BiometricResult.Success, result)
      assertEquals(AppLockState.Unlocked, manager.lockState.value)
    }

  @Test
  fun `a failed prompt stays locked`() =
    runTest {
      val settings = MapSettings().apply { putBoolean("security.app_lock_enabled", true) }
      val manager = AppLockController(FakeAuthenticator(BiometricResult.Cancelled), settings)

      manager.authenticate(prompt)

      assertEquals(AppLockState.Locked, manager.lockState.value)
    }

  @Test
  fun `setEnabled drives the enabled flow`() {
    val manager = AppLockController(FakeAuthenticator(), MapSettings())
    assertFalse(manager.enabled.value)

    manager.setEnabled(true)
    assertTrue(manager.enabled.value)
    assertTrue(manager.isEnabled)
  }

  @Test
  fun `rejects a second prompt while one is in flight`() =
    runTest {
      val gate = CompletableDeferred<Unit>()
      val settings = MapSettings().apply { putBoolean("security.app_lock_enabled", true) }
      val manager = AppLockController(BlockingAuthenticator(gate), settings)

      val first = launch { manager.authenticate(prompt) }
      runCurrent() // first call acquires the gate, then suspends inside the authenticator

      val second = manager.authenticate(prompt)
      assertEquals(BiometricResult.Cancelled, second)

      gate.complete(Unit)
      first.join()
      assertEquals(AppLockState.Unlocked, manager.lockState.value)
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
