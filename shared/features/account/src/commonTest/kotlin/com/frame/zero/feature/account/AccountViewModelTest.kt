package com.frame.zero.feature.account

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.core.security.AppLockController
import com.frame.zero.core.security.BiometricAuthenticator
import com.frame.zero.core.security.BiometricAvailability
import com.frame.zero.core.security.BiometricPromptText
import com.frame.zero.core.security.BiometricResult
import com.frame.zero.core.session.LogoutSignal
import com.frame.zero.core.session.SessionAuthOperations
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.SessionState
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.core.session.UserCache
import com.frame.zero.domain.User
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {
  private val user = User(id = "u1", email = "u@x.com", firstName = "Ada", lastName = "Lovelace")

  @Test
  fun `state reflects logged-in user name and email`() =
    runTest {
      val session = makeSession(this)
      session.onAuthenticated(user)
      val viewModel = AccountViewModel(session, makeLock(), StandardTestDispatcher(testScheduler))

      advanceUntilIdle()

      assertEquals("Ada Lovelace", viewModel.state.value.userName)
      assertEquals("u@x.com", viewModel.state.value.email)
    }

  @Test
  fun `blank last name trims trailing space`() =
    runTest {
      val session = makeSession(this)
      session.onAuthenticated(user.copy(lastName = ""))
      val viewModel = AccountViewModel(session, makeLock(), StandardTestDispatcher(testScheduler))

      advanceUntilIdle()

      assertEquals("Ada", viewModel.state.value.userName)
    }

  @Test
  fun `signOut transitions session to LoggedOut`() =
    runTest {
      val session = makeSession(this)
      session.onAuthenticated(user)
      val viewModel = AccountViewModel(session, makeLock(), StandardTestDispatcher(testScheduler))
      advanceUntilIdle()

      viewModel.signOut()
      advanceUntilIdle()

      assertEquals(SessionState.LoggedOut, session.state.value)
    }

  @Test
  fun `enabling app lock persists after a successful prompt`() =
    runTest {
      val session = makeSession(this)
      val lock = makeLock(authResult = BiometricResult.Success)
      val viewModel = AccountViewModel(session, lock, StandardTestDispatcher(testScheduler))
      advanceUntilIdle()

      viewModel.setAppLockEnabled(enabled = true, prompt = promptText)
      advanceUntilIdle()

      assertTrue(viewModel.state.value.appLockEnabled)
      assertTrue(lock.isEnabled)
    }

  @Test
  fun `a cancelled prompt leaves app lock unchanged`() =
    runTest {
      val session = makeSession(this)
      val lock = makeLock(authResult = BiometricResult.Cancelled)
      val viewModel = AccountViewModel(session, lock, StandardTestDispatcher(testScheduler))
      advanceUntilIdle()

      viewModel.setAppLockEnabled(enabled = true, prompt = promptText)
      advanceUntilIdle()

      assertFalse(viewModel.state.value.appLockEnabled)
      assertFalse(lock.isEnabled)
    }

  @Test
  fun `disabling app lock does not require a prompt`() =
    runTest {
      val session = makeSession(this)
      val lock = makeLock(authResult = BiometricResult.Error("nope"), enabledInitially = true)
      val viewModel = AccountViewModel(session, lock, StandardTestDispatcher(testScheduler))
      advanceUntilIdle()
      assertTrue(viewModel.state.value.appLockEnabled)

      viewModel.setAppLockEnabled(enabled = false, prompt = promptText)
      advanceUntilIdle()

      assertFalse(viewModel.state.value.appLockEnabled)
      assertFalse(lock.isEnabled)
    }

  private val promptText = BiometricPromptText(title = "t", subtitle = "s", negativeButton = "c")

  private fun makeLock(
    authResult: BiometricResult = BiometricResult.Success,
    availability: BiometricAvailability = BiometricAvailability.Available,
    enabledInitially: Boolean = false
  ): AppLockController =
    AppLockController(
      FakeBiometricAuthenticator(authResult, availability),
      MapSettings().apply { if (enabledInitially) putBoolean("security.app_lock_enabled", true) }
    )

  private class FakeBiometricAuthenticator(
    private val authResult: BiometricResult,
    private val availability: BiometricAvailability
  ) : BiometricAuthenticator {
    override fun availability(): BiometricAvailability = availability

    override suspend fun authenticate(prompt: BiometricPromptText): BiometricResult = authResult
  }

  private fun makeSession(scope: TestScope): SessionManager =
    SessionManager(
      tokenStorage = TokenStorage(MapSettings()),
      authOperations = FakeAuthOps,
      userCache = UserCache(MapSettings()),
      logoutSignal = LogoutSignal(),
      scope = scope.backgroundScope
    )

  private object FakeAuthOps : SessionAuthOperations {
    override suspend fun fetchCurrentUser(): UserDto = UserDto(id = "", email = "", firstName = "", lastName = "")

    override suspend fun signOutRemote() = Unit
  }
}
