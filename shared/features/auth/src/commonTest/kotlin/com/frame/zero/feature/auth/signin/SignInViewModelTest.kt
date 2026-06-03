package com.frame.zero.feature.auth.signin

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.core.session.LogoutSignal
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.DomainException
import com.frame.zero.domain.User
import com.frame.zero.feature.auth.domain.LoginUseCase
import com.frame.zero.feature.auth.testing.FakeAuthRepository
import com.frame.zero.feature.auth.testing.NoopSessionAuthOperations
import com.frame.zero.repository.auth.AuthRepository
import com.russhwolf.settings.MapSettings
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
  private val userDto = UserDto(id = "u1", email = "u@x.com", firstName = "", lastName = "")
  private val user = User(id = "u1", email = "u@x.com")

  @Test
  fun `initial state is empty without loading or error`() =
    runTest {
      val vm = makeViewModel(this)

      assertEquals("", vm.state.value.email)
      assertEquals("", vm.state.value.password)
      assertFalse(vm.state.value.isLoading)
      assertNull(vm.state.value.error)
    }

  @Test
  fun `EmailChanged updates email and clears error`() =
    runTest {
      val repo = FakeAuthRepository(loginThrows = DomainException(DomainError.InvalidCredentials))
      val vm = makeViewModel(this, repo)
      vm.onIntent(SignInIntent.EmailChanged("u@x.com"))
      vm.onIntent(SignInIntent.PasswordChanged("wrong"))
      vm.onIntent(SignInIntent.Submit)
      advanceUntilIdle()

      vm.onIntent(SignInIntent.EmailChanged("v@x.com"))

      assertEquals("v@x.com", vm.state.value.email)
      assertNull(vm.state.value.error)
    }

  @Test
  fun `Submit with blank email sets validation error and skips repository`() =
    runTest {
      val repo = FakeAuthRepository(loginUserDto = userDto)
      val vm = makeViewModel(this, repo)

      vm.onIntent(SignInIntent.PasswordChanged("p"))
      vm.onIntent(SignInIntent.Submit)
      advanceUntilIdle()

      assertEquals("Email and password must not be empty", vm.state.value.error)
      assertEquals(0, repo.loginCalls.size)
    }

  @Test
  fun `Submit with blank password sets validation error`() =
    runTest {
      val repo = FakeAuthRepository(loginUserDto = userDto)
      val vm = makeViewModel(this, repo)

      vm.onIntent(SignInIntent.EmailChanged("u@x.com"))
      vm.onIntent(SignInIntent.Submit)
      advanceUntilIdle()

      assertEquals("Email and password must not be empty", vm.state.value.error)
    }

  @Test
  fun `successful Submit clears loading and error`() =
    runTest {
      val repo = FakeAuthRepository(loginUserDto = userDto)
      val vm = makeViewModel(this, repo)

      vm.onIntent(SignInIntent.EmailChanged("u@x.com"))
      vm.onIntent(SignInIntent.PasswordChanged("p"))
      vm.onIntent(SignInIntent.Submit)
      advanceUntilIdle()

      assertFalse(vm.state.value.isLoading)
      assertNull(vm.state.value.error)
    }

  @Test
  fun `failed Submit surfaces InvalidCredentials message`() =
    runTest {
      val repo = FakeAuthRepository(loginThrows = DomainException(DomainError.InvalidCredentials))
      val vm = makeViewModel(this, repo)

      vm.onIntent(SignInIntent.EmailChanged("u@x.com"))
      vm.onIntent(SignInIntent.PasswordChanged("wrong"))
      vm.onIntent(SignInIntent.Submit)
      advanceUntilIdle()

      assertEquals("Invalid email or password", vm.state.value.error)
      assertFalse(vm.state.value.isLoading)
    }

  @Test
  fun `Network error surfaces as a toast, not an inline error`() =
    runTest {
      val repo = FakeAuthRepository(loginThrows = DomainException(DomainError.Network("offline")))
      val vm = makeViewModel(this, repo)

      vm.onIntent(SignInIntent.EmailChanged("u@x.com"))
      vm.onIntent(SignInIntent.PasswordChanged("p"))
      vm.onIntent(SignInIntent.Submit)
      advanceUntilIdle()

      assertEquals("Network error: offline", vm.state.value.errorToast)
      assertNull(vm.state.value.error)
    }

  @Test
  fun `Unknown server error surfaces as a toast with fallback text`() =
    runTest {
      val repo = FakeAuthRepository(loginThrows = DomainException(DomainError.Unknown(null)))
      val vm = makeViewModel(this, repo)

      vm.onIntent(SignInIntent.EmailChanged("u@x.com"))
      vm.onIntent(SignInIntent.PasswordChanged("p"))
      vm.onIntent(SignInIntent.Submit)
      advanceUntilIdle()

      assertEquals("Something went wrong", vm.state.value.errorToast)
      assertNull(vm.state.value.error)
    }

  @Test
  fun `ToastDismissed clears the toast message`() =
    runTest {
      val repo = FakeAuthRepository(loginThrows = DomainException(DomainError.Network("offline")))
      val vm = makeViewModel(this, repo)

      vm.onIntent(SignInIntent.EmailChanged("u@x.com"))
      vm.onIntent(SignInIntent.PasswordChanged("p"))
      vm.onIntent(SignInIntent.Submit)
      advanceUntilIdle()
      assertEquals("Network error: offline", vm.state.value.errorToast)

      vm.onIntent(SignInIntent.ToastDismissed)

      assertNull(vm.state.value.errorToast)
    }

  @Test
  fun `second Submit while the first is in flight is ignored`() =
    runTest {
      val gate = CompletableDeferred<UserDto>()
      val repo = GatedAuthRepository(loginGate = gate)
      val vm = makeViewModel(this, repo)

      vm.onIntent(SignInIntent.EmailChanged("u@x.com"))
      vm.onIntent(SignInIntent.PasswordChanged("p"))
      vm.onIntent(SignInIntent.Submit)
      advanceUntilIdle()
      assertTrue(vm.state.value.isLoading)
      assertEquals(1, repo.loginInvocations)

      vm.onIntent(SignInIntent.Submit)
      advanceUntilIdle()

      assertEquals(1, repo.loginInvocations)

      gate.complete(userDto)
      advanceUntilIdle()
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
        logoutSignal = LogoutSignal(),
        scope = scope.backgroundScope
      )
    return SignInViewModel(
      loginUseCase = LoginUseCase(repo, sessionManager),
      dispatcher = StandardTestDispatcher(scope.testScheduler)
    )
  }

  private class GatedAuthRepository(
    private val loginGate: CompletableDeferred<UserDto>
  ) : AuthRepository {
    var loginInvocations: Int = 0
      private set

    override suspend fun login(
      email: String,
      password: String
    ): UserDto {
      loginInvocations++
      return loginGate.await()
    }

    override suspend fun register(
      email: String,
      password: String,
      firstName: String,
      lastName: String
    ): UserDto = error("not expected")

    override suspend fun logout() = Unit

    override suspend fun getCurrentUser(): UserDto = error("not expected")
  }
}
