package com.frame.zero.repository.device_token

import com.frame.zero.core.logging.LoggerImpl
import com.frame.zero.core.session.SessionState
import com.frame.zero.domain.User
import com.frame.zero.repository.device_token.testing.FakeDeviceTokenRepository
import com.frame.zero.repository.device_token.testing.FakePushTokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeviceTokenSynchronizerTest {
  private val loggedIn = SessionState.LoggedIn(User(id = "u1", email = "u@x.com"))

  @Test
  fun `registers the current token when the session becomes logged in`() =
    runTest {
      val state = MutableStateFlow<SessionState>(SessionState.LoggedOut)
      val repo = FakeDeviceTokenRepository()
      val scope = registrarScope()
      // Unconfined: the collector subscribes (and reacts) eagerly and synchronously.
      DeviceTokenSynchronizer(state, FakePushTokenProvider("tok-1"), repo, noopLogger, scope = scope)

      state.value = loggedIn

      assertEquals(1, repo.registered.size)
      assertEquals("tok-1", repo.registered.single().token)
      scope.cancel()
    }

  @Test
  fun `does not register while logged out`() =
    runTest {
      val state = MutableStateFlow<SessionState>(SessionState.LoggedOut)
      val repo = FakeDeviceTokenRepository()
      val scope = registrarScope()
      DeviceTokenSynchronizer(state, FakePushTokenProvider("tok-1"), repo, noopLogger, scope = scope)

      assertTrue(repo.registered.isEmpty())
      scope.cancel()
    }

  @Test
  fun `onNewToken registers a rotated token when logged in`() =
    runTest {
      val state = MutableStateFlow<SessionState>(loggedIn)
      val repo = FakeDeviceTokenRepository()
      val scope = registrarScope()
      // No current token, so the login-triggered registration is a no-op and we
      // observe only the rotated token.
      val synchronizer =
        DeviceTokenSynchronizer(state, FakePushTokenProvider(token = null), repo, noopLogger, scope = scope)

      synchronizer.onNewToken("rotated")

      assertEquals(listOf("rotated"), repo.registered.map { it.token })
      scope.cancel()
    }

  @Test
  fun `onNewToken is ignored while logged out`() =
    runTest {
      val state = MutableStateFlow<SessionState>(SessionState.LoggedOut)
      val repo = FakeDeviceTokenRepository()
      val scope = registrarScope()
      val synchronizer =
        DeviceTokenSynchronizer(state, FakePushTokenProvider(token = null), repo, noopLogger, scope = scope)

      synchronizer.onNewToken("rotated")

      assertTrue(repo.registered.isEmpty())
      scope.cancel()
    }

  private val noopLogger = LoggerImpl(emptyList())

  private fun kotlinx.coroutines.test.TestScope.registrarScope(): CoroutineScope =
    CoroutineScope(UnconfinedTestDispatcher(testScheduler))
}
