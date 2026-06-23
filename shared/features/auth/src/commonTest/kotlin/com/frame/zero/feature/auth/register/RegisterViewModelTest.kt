package com.frame.zero.feature.auth.register

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.core.session.LogoutSignal
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.core.session.UserCache
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.DomainException
import com.frame.zero.feature.auth.domain.RegisterUseCase
import com.frame.zero.testing.FakeAuthRepository
import com.frame.zero.testing.NoopSessionAuthOperations
import com.frame.zero.repository.auth.AuthRepository
import com.frame.zero.ui.asUiText
import com.russhwolf.settings.MapSettings
import framezero.shared.features.auth.generated.resources.Res
import framezero.shared.features.auth.generated.resources.error_email_exists
import framezero.shared.features.auth.generated.resources.error_empty_credentials
import framezero.shared.features.auth.generated.resources.error_network
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {
  private val userDto = UserDto(id = "u1", email = "u@x.com", firstName = "", lastName = "")

  @Test
  fun `initial state is empty without loading or error`() =
    runTest {
      val vm = makeViewModel(this)

      assertEquals("", vm.state.value.firstName)
      assertEquals("", vm.state.value.lastName)
      assertEquals("", vm.state.value.email)
      assertEquals("", vm.state.value.password)
      assertFalse(vm.state.value.isLoading)
      assertNull(vm.state.value.error)
    }

  @Test
  fun `FirstNameChanged updates firstName only`() =
    runTest {
      val vm = makeViewModel(this)

      vm.onIntent(RegisterIntent.FirstNameChanged("Jane"))

      assertEquals("Jane", vm.state.value.firstName)
      assertEquals("", vm.state.value.email)
    }

  @Test
  fun `LastNameChanged updates lastName only`() =
    runTest {
      val vm = makeViewModel(this)

      vm.onIntent(RegisterIntent.LastNameChanged("Doe"))

      assertEquals("Doe", vm.state.value.lastName)
      assertEquals("", vm.state.value.email)
    }

  @Test
  fun `Submit with blank fields sets validation error and skips repository`() =
    runTest {
      val repo = FakeAuthRepository(registerUserDto = userDto)
      val vm = makeViewModel(this, repo)

      vm.onIntent(RegisterIntent.Submit)
      advanceUntilIdle()

      assertEquals(Res.string.error_empty_credentials.asUiText(), vm.state.value.error)
      assertEquals(0, repo.registerCalls.size)
    }

  @Test
  fun `successful Submit clears loading and error`() =
    runTest {
      val repo = FakeAuthRepository(registerUserDto = userDto)
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
  fun `failed Submit surfaces EmailAlreadyExists message`() =
    runTest {
      val repo =
        FakeAuthRepository(registerThrows = DomainException(DomainError.EmailAlreadyExists))
      val vm = makeViewModel(this, repo)

      vm.onIntent(RegisterIntent.EmailChanged("dup@x.com"))
      vm.onIntent(RegisterIntent.PasswordChanged("p"))
      vm.onIntent(RegisterIntent.Submit)
      advanceUntilIdle()

      assertEquals(Res.string.error_email_exists.asUiText(), vm.state.value.error)
    }

  @Test
  fun `Network error surfaces as a toast instead of an inline error`() =
    runTest {
      val repo = FakeAuthRepository(registerThrows = DomainException(DomainError.Offline("offline")))
      val vm = makeViewModel(this, repo)

      vm.onIntent(RegisterIntent.EmailChanged("u@x.com"))
      vm.onIntent(RegisterIntent.PasswordChanged("p"))
      vm.onIntent(RegisterIntent.Submit)
      advanceUntilIdle()

      assertEquals(Res.string.error_network.asUiText("offline"), vm.state.value.errorToast)
      assertNull(vm.state.value.error)
    }

  @Test
  fun `ToastDismissed clears the toast message`() =
    runTest {
      val repo = FakeAuthRepository(registerThrows = DomainException(DomainError.Offline("offline")))
      val vm = makeViewModel(this, repo)

      vm.onIntent(RegisterIntent.EmailChanged("u@x.com"))
      vm.onIntent(RegisterIntent.PasswordChanged("p"))
      vm.onIntent(RegisterIntent.Submit)
      advanceUntilIdle()
      assertEquals(Res.string.error_network.asUiText("offline"), vm.state.value.errorToast)

      vm.onIntent(RegisterIntent.ToastDismissed)

      assertNull(vm.state.value.errorToast)
    }

  @Test
  fun `Submit routes to register and not login`() =
    runTest {
      val repo = FakeAuthRepository(registerUserDto = userDto)
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
    repo: AuthRepository = FakeAuthRepository()
  ): RegisterViewModel {
    val sessionManager =
      SessionManager(
        tokenStorage = TokenStorage(MapSettings()),
        authOperations = NoopSessionAuthOperations,
        userCache = UserCache(MapSettings()),
        logoutSignal = LogoutSignal(),
        scope = scope.backgroundScope
      )
    return RegisterViewModel(
      registerUseCase = RegisterUseCase(repo, sessionManager),
      dispatcher = StandardTestDispatcher(scope.testScheduler)
    )
  }
}
