package com.frame.zero.feature.auth

import com.frame.zero.core.session.LogoutSignal
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.User
import com.frame.zero.feature.auth.testing.FakeAuthRepository
import com.frame.zero.feature.auth.testing.NoopSessionAuthOperations
import com.frame.zero.feature.auth.usecase.LoginUseCase
import com.frame.zero.feature.auth.usecase.RegisterUseCase
import com.frame.zero.repository.auth.AuthRepository
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

  private val user = User(id = "u1", email = "u@x.com")

  @Test
  fun `initial state is Login mode without loading or error`() = runTest {
    val vm = makeViewModel(this)

    assertEquals(AuthMode.Login, vm.state.value.mode)
    assertFalse(vm.state.value.isLoading)
    assertNull(vm.state.value.error)
  }

  @Test
  fun `SwitchMode toggles to Register and back`() = runTest {
    val vm = makeViewModel(this)

    vm.onIntent(AuthIntent.SwitchMode)
    assertEquals(AuthMode.Register, vm.state.value.mode)

    vm.onIntent(AuthIntent.SwitchMode)
    assertEquals(AuthMode.Login, vm.state.value.mode)
  }

  @Test
  fun `SwitchMode clears the existing error`() = runTest {
    val repo = FakeAuthRepository(loginResult = Outcome.Failure(DomainError.InvalidCredentials))
    val vm = makeViewModel(this, repo)
    vm.onIntent(AuthIntent.Login(email = "u@x.com", password = "wrong"))
    advanceUntilIdle()
    assertNotNull(vm.state.value.error)

    vm.onIntent(AuthIntent.SwitchMode)

    assertNull(vm.state.value.error)
  }

  @Test
  fun `Login with blank email sets validation error and skips repository`() = runTest {
    val repo = FakeAuthRepository(loginResult = Outcome.Success(user))
    val vm = makeViewModel(this, repo)

    vm.onIntent(AuthIntent.Login(email = "", password = "p"))
    advanceUntilIdle()

    assertEquals("Email and password must not be empty", vm.state.value.error)
    assertEquals(0, repo.loginCalls.size)
  }

  @Test
  fun `Login with blank password sets validation error`() = runTest {
    val repo = FakeAuthRepository(loginResult = Outcome.Success(user))
    val vm = makeViewModel(this, repo)

    vm.onIntent(AuthIntent.Login(email = "u@x.com", password = ""))
    advanceUntilIdle()

    assertEquals("Email and password must not be empty", vm.state.value.error)
  }

  @Test
  fun `Register with blank fields sets validation error and skips repository`() = runTest {
    val repo = FakeAuthRepository(registerResult = Outcome.Success(user))
    val vm = makeViewModel(this, repo)

    vm.onIntent(AuthIntent.Register(email = "", password = ""))
    advanceUntilIdle()

    assertEquals("Email and password must not be empty", vm.state.value.error)
    assertEquals(0, repo.registerCalls.size)
  }

  @Test
  fun `successful Login clears loading and error`() = runTest {
    val repo = FakeAuthRepository(loginResult = Outcome.Success(user))
    val vm = makeViewModel(this, repo)

    vm.onIntent(AuthIntent.Login(email = "u@x.com", password = "p"))
    advanceUntilIdle()

    assertFalse(vm.state.value.isLoading)
    assertNull(vm.state.value.error)
  }

  @Test
  fun `failed Login surfaces InvalidCredentials message`() = runTest {
    val repo = FakeAuthRepository(loginResult = Outcome.Failure(DomainError.InvalidCredentials))
    val vm = makeViewModel(this, repo)

    vm.onIntent(AuthIntent.Login(email = "u@x.com", password = "wrong"))
    advanceUntilIdle()

    assertEquals("Invalid email or password", vm.state.value.error)
    assertFalse(vm.state.value.isLoading)
  }

  @Test
  fun `failed Register surfaces EmailAlreadyExists message`() = runTest {
    val repo = FakeAuthRepository(registerResult = Outcome.Failure(DomainError.EmailAlreadyExists))
    val vm = makeViewModel(this, repo)

    vm.onIntent(AuthIntent.Register(email = "dup@x.com", password = "p"))
    advanceUntilIdle()

    assertEquals("An account with this email already exists", vm.state.value.error)
  }

  @Test
  fun `Network error message includes the underlying detail`() = runTest {
    val repo = FakeAuthRepository(loginResult = Outcome.Failure(DomainError.Network("offline")))
    val vm = makeViewModel(this, repo)

    vm.onIntent(AuthIntent.Login(email = "u@x.com", password = "p"))
    advanceUntilIdle()

    assertEquals("Network error: offline", vm.state.value.error)
  }

  @Test
  fun `Unknown error with null message uses fallback text`() = runTest {
    val repo = FakeAuthRepository(loginResult = Outcome.Failure(DomainError.Unknown(null)))
    val vm = makeViewModel(this, repo)

    vm.onIntent(AuthIntent.Login(email = "u@x.com", password = "p"))
    advanceUntilIdle()

    assertEquals("Something went wrong", vm.state.value.error)
  }

  @Test
  fun `Unknown error with message surfaces that message`() = runTest {
    val repo = FakeAuthRepository(loginResult = Outcome.Failure(DomainError.Unknown("boom")))
    val vm = makeViewModel(this, repo)

    vm.onIntent(AuthIntent.Login(email = "u@x.com", password = "p"))
    advanceUntilIdle()

    assertEquals("boom", vm.state.value.error)
  }

  @Test
  fun `Register routes to register use case and not login`() = runTest {
    val repo =
      FakeAuthRepository(
        loginResult = Outcome.Failure(DomainError.Unknown(null)),
        registerResult = Outcome.Success(user),
      )
    val vm = makeViewModel(this, repo)

    vm.onIntent(AuthIntent.Register(email = "u@x.com", password = "p"))
    advanceUntilIdle()

    assertEquals(0, repo.loginCalls.size)
    assertEquals(1, repo.registerCalls.size)
  }

  @Test
  fun `second Login while the first is in flight is ignored`() = runTest {
    val gate = CompletableDeferred<Outcome<User>>()
    val repo = GatedAuthRepository(loginGate = gate)
    val vm = makeViewModel(this, repo)

    vm.onIntent(AuthIntent.Login(email = "u@x.com", password = "p"))
    advanceUntilIdle()
    assertTrue(vm.state.value.isLoading)
    assertEquals(1, repo.loginInvocations)

    vm.onIntent(AuthIntent.Login(email = "other@x.com", password = "other"))
    advanceUntilIdle()

    assertEquals(1, repo.loginInvocations)

    // Unblock the in-flight call so runTest can complete cleanly.
    gate.complete(Outcome.Success(user))
    advanceUntilIdle()
  }

  // -- helpers ---------------------------------------------------------------

  private fun makeViewModel(
    scope: TestScope,
    repo: AuthRepository = FakeAuthRepository(),
  ): AuthViewModel {
    val sessionManager =
      SessionManager(
        tokenStorage = TokenStorage(MapSettings()),
        authOperations = NoopSessionAuthOperations,
        logoutSignal = LogoutSignal(),
        scope = scope.backgroundScope,
      )
    return AuthViewModel(
      loginUseCase = LoginUseCase(repo, sessionManager),
      registerUseCase = RegisterUseCase(repo, sessionManager),
      dispatcher = StandardTestDispatcher(scope.testScheduler),
    )
  }

  private class GatedAuthRepository(private val loginGate: CompletableDeferred<Outcome<User>>) :
    AuthRepository {
    var loginInvocations: Int = 0
      private set

    override suspend fun login(email: String, password: String): Outcome<User> {
      loginInvocations++
      return loginGate.await()
    }

    override suspend fun register(email: String, password: String): Outcome<User> =
      Outcome.Failure(DomainError.Unknown(null))

    override suspend fun logout(): Outcome<Unit> = Outcome.Success(Unit)

    override suspend fun getCurrentUser(): Outcome<User> =
      Outcome.Failure(DomainError.Unknown(null))
  }
}
