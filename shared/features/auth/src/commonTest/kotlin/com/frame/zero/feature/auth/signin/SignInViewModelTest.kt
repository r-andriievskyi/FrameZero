package com.frame.zero.feature.auth.signin

import app.cash.turbine.test
import com.frame.zero.core.session.LogoutSignal
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.core.session.UserCache
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.DomainException
import com.frame.zero.domain.User
import com.frame.zero.feature.auth.domain.LoginUseCase
import com.frame.zero.testing.FakeAuthRepository
import com.frame.zero.testing.NoopSessionAuthOperations
import com.frame.zero.repository.auth.AuthRepository
import com.frame.zero.ui.asUiText
import com.russhwolf.settings.MapSettings
import framezero.shared.features.auth.generated.resources.Res
import framezero.shared.features.auth.generated.resources.error_empty_credentials
import framezero.shared.features.auth.generated.resources.error_invalid_credentials
import framezero.shared.ui_text.generated.resources.Res as UiTextRes
import framezero.shared.ui_text.generated.resources.error_network
import framezero.shared.ui_text.generated.resources.error_unknown_fallback
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {
  private val user = User(id = "u1", email = "u@x.com")

  @Test
  fun `initial state is empty without loading or error`() =
    runTest {
      val vm = makeViewModel(this)

      vm.state.test {
        val state = awaitItem()
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertNull(state.error)
      }
    }

  @Test
  fun `EmailChanged updates email and clears error`() =
    runTest {
      val repo = FakeAuthRepository(loginThrows = DomainException(DomainError.InvalidCredentials))
      val vm = makeViewModel(this, repo)

      vm.state.test {
        vm.onIntent(SignInIntent.EmailChanged("u@x.com"))
        vm.onIntent(SignInIntent.PasswordChanged("wrong"))
        vm.onIntent(SignInIntent.Submit)
        advanceUntilIdle()

        vm.onIntent(SignInIntent.EmailChanged("v@x.com"))
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertEquals("v@x.com", state.email)
        assertNull(state.error)
      }
    }

  @Test
  fun `Submit with blank email sets validation error and skips repository`() =
    runTest {
      val repo = FakeAuthRepository(loginUser = user)
      val vm = makeViewModel(this, repo)

      vm.state.test {
        vm.onIntent(SignInIntent.PasswordChanged("p"))
        vm.onIntent(SignInIntent.Submit)
        advanceUntilIdle()

        assertEquals(Res.string.error_empty_credentials.asUiText(), expectMostRecentItem().error)
      }
      assertEquals(0, repo.loginCalls.size)
    }

  @Test
  fun `Submit with blank password sets validation error`() =
    runTest {
      val repo = FakeAuthRepository(loginUser = user)
      val vm = makeViewModel(this, repo)

      vm.state.test {
        vm.onIntent(SignInIntent.EmailChanged("u@x.com"))
        vm.onIntent(SignInIntent.Submit)
        advanceUntilIdle()

        assertEquals(Res.string.error_empty_credentials.asUiText(), expectMostRecentItem().error)
      }
    }

  @Test
  fun `successful Submit clears loading and error`() =
    runTest {
      val repo = FakeAuthRepository(loginUser = user)
      val vm = makeViewModel(this, repo)

      vm.state.test {
        vm.onIntent(SignInIntent.EmailChanged("u@x.com"))
        vm.onIntent(SignInIntent.PasswordChanged("p"))
        vm.onIntent(SignInIntent.Submit)
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertFalse(state.isLoading)
        assertNull(state.error)
      }
    }

  @Test
  fun `failed Submit surfaces InvalidCredentials message`() =
    runTest {
      val repo = FakeAuthRepository(loginThrows = DomainException(DomainError.InvalidCredentials))
      val vm = makeViewModel(this, repo)

      vm.state.test {
        vm.onIntent(SignInIntent.EmailChanged("u@x.com"))
        vm.onIntent(SignInIntent.PasswordChanged("wrong"))
        vm.onIntent(SignInIntent.Submit)
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertEquals(Res.string.error_invalid_credentials.asUiText(), state.error)
        assertFalse(state.isLoading)
      }
    }

  @Test
  fun `Network error surfaces as a toast instead of an inline error`() =
    runTest {
      val repo = FakeAuthRepository(loginThrows = DomainException(DomainError.Offline))
      val vm = makeViewModel(this, repo)

      vm.state.test {
        vm.onIntent(SignInIntent.EmailChanged("u@x.com"))
        vm.onIntent(SignInIntent.PasswordChanged("p"))
        vm.onIntent(SignInIntent.Submit)
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertEquals(UiTextRes.string.error_network.asUiText(), state.errorToast)
        assertNull(state.error)
      }
    }

  @Test
  fun `Unknown server error surfaces as a toast with fallback text`() =
    runTest {
      val repo = FakeAuthRepository(loginThrows = DomainException(DomainError.Unknown))
      val vm = makeViewModel(this, repo)

      vm.state.test {
        vm.onIntent(SignInIntent.EmailChanged("u@x.com"))
        vm.onIntent(SignInIntent.PasswordChanged("p"))
        vm.onIntent(SignInIntent.Submit)
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertEquals(UiTextRes.string.error_unknown_fallback.asUiText(), state.errorToast)
        assertNull(state.error)
      }
    }

  @Test
  fun `ToastDismissed clears the toast message`() =
    runTest {
      val repo = FakeAuthRepository(loginThrows = DomainException(DomainError.Offline))
      val vm = makeViewModel(this, repo)

      vm.state.test {
        vm.onIntent(SignInIntent.EmailChanged("u@x.com"))
        vm.onIntent(SignInIntent.PasswordChanged("p"))
        vm.onIntent(SignInIntent.Submit)
        advanceUntilIdle()
        assertEquals(UiTextRes.string.error_network.asUiText(), expectMostRecentItem().errorToast)

        vm.onIntent(SignInIntent.ToastDismissed)
        advanceUntilIdle()
        assertNull(expectMostRecentItem().errorToast)
      }
    }

  @Test
  fun `second Submit while the first is in flight is ignored`() =
    runTest {
      val gate = CompletableDeferred<User>()
      val repo = GatedAuthRepository(loginGate = gate)
      val vm = makeViewModel(this, repo)

      vm.state.test {
        vm.onIntent(SignInIntent.EmailChanged("u@x.com"))
        vm.onIntent(SignInIntent.PasswordChanged("p"))
        vm.onIntent(SignInIntent.Submit)
        advanceUntilIdle()
        assertTrue(expectMostRecentItem().isLoading)
        assertEquals(1, repo.loginInvocations)

        vm.onIntent(SignInIntent.Submit)
        advanceUntilIdle()

        assertEquals(1, repo.loginInvocations)

        gate.complete(user)
        advanceUntilIdle()
        cancelAndIgnoreRemainingEvents()
      }
    }

  // -- helpers ---------------------------------------------------------------

  private fun makeViewModel(
    scope: TestScope,
    repo: AuthRepository = FakeAuthRepository()
  ): SignInViewModel {
    val sessionManager =
      SessionManager(
        tokenStorage = TokenStorage(MapSettings()),
        authOperations = NoopSessionAuthOperations,
        userCache = UserCache(MapSettings()),
        logoutSignal = LogoutSignal(),
        scope = scope.backgroundScope
      )
    return SignInViewModel(
      loginUseCase = LoginUseCase(repo, sessionManager),
      dispatcher = StandardTestDispatcher(scope.testScheduler)
    )
  }

  private class GatedAuthRepository(
    private val loginGate: CompletableDeferred<User>
  ) : AuthRepository {
    var loginInvocations: Int = 0
      private set

    override suspend fun login(
      email: String,
      password: String
    ): User {
      loginInvocations++
      return loginGate.await()
    }

    override suspend fun register(
      email: String,
      password: String,
      firstName: String,
      lastName: String
    ): User = error("not expected")

    override suspend fun logout() = Unit

    override suspend fun getCurrentUser(): User = error("not expected")
  }
}
