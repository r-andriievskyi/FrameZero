package com.frame.zero.feature.auth.usecase

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
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class LoginUseCaseTest {
  private val user = User(id = "u1", email = "u@x.com")

  @Test
  fun `success transitions session to LoggedIn`() =
    runTest {
      val repo = FakeAuthRepository(loginUser = user)
      val session = makeSessionManager()

      session.state.test {
        val loginUseCase = LoginUseCase(repo, session)
        val outcome = loginUseCase(LoginUseCase.Params(email = "u@x.com", password = "p"))
        advanceUntilIdle()

        val success = assertIs<Outcome.Success<User>>(outcome)
        assertEquals(user, success.data)
        assertEquals(SessionState.LoggedIn(user), expectMostRecentItem())
      }
    }

  @Test
  fun `failure leaves session state untouched`() =
    runTest {
      val repo = FakeAuthRepository(loginThrows = DomainException(DomainError.InvalidCredentials))
      val session = makeSessionManager()
      val before = session.state.value

      session.state.test {
        val outcome =
          LoginUseCase(repo, session)(LoginUseCase.Params(email = "u@x.com", password = "wrong"))
        advanceUntilIdle()

        assertIs<Outcome.Failure>(outcome)
        assertEquals(before, expectMostRecentItem())
      }
    }

  @Test
  fun `forwards email and password to repository`() =
    runTest {
      val repo = FakeAuthRepository(loginUser = user)

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
