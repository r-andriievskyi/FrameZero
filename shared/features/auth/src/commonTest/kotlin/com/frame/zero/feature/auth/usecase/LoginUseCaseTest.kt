package com.frame.zero.feature.auth.usecase

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.core.session.LogoutSignal
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.SessionState
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.core.session.UserCache
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.DomainException
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.User
import com.frame.zero.feature.auth.domain.LoginUseCase
import com.frame.zero.testing.FakeAuthRepository
import com.frame.zero.testing.NoopSessionAuthOperations
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LoginUseCaseTest {
  private val userDto = UserDto(id = "u1", email = "u@x.com", firstName = "", lastName = "")
  private val user = User(id = "u1", email = "u@x.com")

  @Test
  fun `success transitions session to LoggedIn`() =
    runTest {
      val repo = FakeAuthRepository(loginUserDto = userDto)
      val session = makeSessionManager()

      val loginUseCase = LoginUseCase(repo, session)
      val outcome = loginUseCase(LoginUseCase.Params(email = "u@x.com", password = "p"))

      val success = assertIs<Outcome.Success<User>>(outcome)
      assertEquals(user, success.data)
      assertEquals(SessionState.LoggedIn(user), session.state.value)
    }

  @Test
  fun `failure leaves session state untouched`() =
    runTest {
      val repo = FakeAuthRepository(loginThrows = DomainException(DomainError.InvalidCredentials))
      val session = makeSessionManager()
      val before = session.state.value

      val outcome =
        LoginUseCase(repo, session)(LoginUseCase.Params(email = "u@x.com", password = "wrong"))

      assertIs<Outcome.Failure>(outcome)
      assertEquals(before, session.state.value)
    }

  @Test
  fun `forwards email and password to repository`() =
    runTest {
      val repo = FakeAuthRepository(loginUserDto = userDto)

      LoginUseCase(repo, makeSessionManager())(
        LoginUseCase.Params(email = "typed@x.com", password = "secret")
      )

      assertEquals(listOf("typed@x.com" to "secret"), repo.loginCalls)
    }

  private fun TestScope.makeSessionManager(): SessionManager =
    SessionManager(
      tokenStorage = TokenStorage(MapSettings()),
      authOperations = NoopSessionAuthOperations,
      userCache = UserCache(MapSettings()),
      logoutSignal = LogoutSignal(),
      scope = backgroundScope
    )
}
