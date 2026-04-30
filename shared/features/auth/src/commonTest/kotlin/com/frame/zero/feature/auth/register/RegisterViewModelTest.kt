package com.frame.zero.feature.auth.register

import com.frame.zero.core.session.LogoutSignal
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.User
import com.frame.zero.feature.auth.testing.FakeAuthRepository
import com.frame.zero.feature.auth.testing.NoopSessionAuthOperations
import com.frame.zero.feature.auth.usecase.RegisterUseCase
import com.frame.zero.repository.auth.AuthRepository
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

  private val user = User(id = "u1", email = "u@x.com")

  @Test
  fun `initial state is empty without loading or error`() = runTest {
    val vm = makeViewModel(this)

    assertEquals("", vm.state.value.name)
    assertEquals("", vm.state.value.email)
    assertEquals("", vm.state.value.password)
    assertFalse(vm.state.value.isLoading)
    assertNull(vm.state.value.error)
  }

  @Test
  fun `NameChanged updates name only`() = runTest {
    val vm = makeViewModel(this)

    vm.onIntent(RegisterIntent.NameChanged("Jane"))

    assertEquals("Jane", vm.state.value.name)
    assertEquals("", vm.state.value.email)
  }

  @Test
  fun `Submit with blank fields sets validation error and skips repository`() = runTest {
    val repo = FakeAuthRepository(registerResult = Outcome.Success(user))
    val vm = makeViewModel(this, repo)

    vm.onIntent(RegisterIntent.Submit)
    advanceUntilIdle()

    assertEquals("Email and password must not be empty", vm.state.value.error)
    assertEquals(0, repo.registerCalls.size)
  }

  @Test
  fun `successful Submit clears loading and error`() = runTest {
    val repo = FakeAuthRepository(registerResult = Outcome.Success(user))
    val vm = makeViewModel(this, repo)

    vm.onIntent(RegisterIntent.EmailChanged("u@x.com"))
    vm.onIntent(RegisterIntent.PasswordChanged("p"))
    vm.onIntent(RegisterIntent.Submit)
    advanceUntilIdle()

    assertFalse(vm.state.value.isLoading)
    assertNull(vm.state.value.error)
    assertEquals(1, repo.registerCalls.size)
  }

  @Test
  fun `failed Submit surfaces EmailAlreadyExists message`() = runTest {
    val repo = FakeAuthRepository(registerResult = Outcome.Failure(DomainError.EmailAlreadyExists))
    val vm = makeViewModel(this, repo)

    vm.onIntent(RegisterIntent.EmailChanged("dup@x.com"))
    vm.onIntent(RegisterIntent.PasswordChanged("p"))
    vm.onIntent(RegisterIntent.Submit)
    advanceUntilIdle()

    assertEquals("An account with this email already exists", vm.state.value.error)
  }

  @Test
  fun `Submit routes to register and not login`() = runTest {
    val repo =
      FakeAuthRepository(
        loginResult = Outcome.Failure(DomainError.Unknown(null)),
        registerResult = Outcome.Success(user),
      )
    val vm = makeViewModel(this, repo)

    vm.onIntent(RegisterIntent.EmailChanged("u@x.com"))
    vm.onIntent(RegisterIntent.PasswordChanged("p"))
    vm.onIntent(RegisterIntent.Submit)
    advanceUntilIdle()

    assertEquals(0, repo.loginCalls.size)
    assertEquals(1, repo.registerCalls.size)
  }

  // -- helpers ---------------------------------------------------------------

  private fun makeViewModel(
    scope: TestScope,
    repo: AuthRepository = FakeAuthRepository(),
  ): RegisterViewModel {
    val sessionManager =
      SessionManager(
        tokenStorage = TokenStorage(MapSettings()),
        authOperations = NoopSessionAuthOperations,
        logoutSignal = LogoutSignal(),
        scope = scope.backgroundScope,
      )
    return RegisterViewModel(
      registerUseCase = RegisterUseCase(repo, sessionManager),
      dispatcher = StandardTestDispatcher(scope.testScheduler),
    )
  }
}
