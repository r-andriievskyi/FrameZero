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
import com.frame.zero.feature.auth.domain.RegisterUseCase
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
class RegisterUseCaseTest {
  private val user = User(id = "u1", email = "new@x.com")

  @Test
  fun `success transitions session to LoggedIn`() =
    runTest {
      val repo = FakeAuthRepository(registerUser = user)
      val session = makeSessionManager()

      session.state.test {
        val outcome =
          RegisterUseCase(repo, session)(
            RegisterUseCase.Params(
              email = "new@x.com",
              password = "p",
              firstName = "Jane",
              lastName = "Doe"
            )
          )
        advanceUntilIdle()

        val success = assertIs<Outcome.Success<User>>(outcome)
        assertEquals(user, success.data)
        assertEquals(SessionState.LoggedIn(user), expectMostRecentItem())
      }
    }

  @Test
  fun `failure leaves session state untouched`() =
    runTest {
      val repo =
        FakeAuthRepository(registerThrows = DomainException(DomainError.EmailAlreadyExists))
      val session = makeSessionManager()
      val before = session.state.value

      session.state.test {
        val outcome =
          RegisterUseCase(repo, session)(
            RegisterUseCase.Params(email = "dup@x.com", password = "p", firstName = "", lastName = "")
          )
        advanceUntilIdle()

        assertIs<Outcome.Failure>(outcome)
        assertEquals(before, expectMostRecentItem())
      }
    }

  @Test
  fun `forwards all fields to repository`() =
    runTest {
      val repo = FakeAuthRepository(registerUser = user)

      RegisterUseCase(repo, makeSessionManager())(
        RegisterUseCase.Params(
          email = "typed@x.com",
          password = "secret",
          firstName = "Jane",
          lastName = "Doe"
        )
      )

      val call = repo.registerCalls.single()
      assertEquals("typed@x.com", call.email)
      assertEquals("secret", call.password)
      assertEquals("Jane", call.firstName)
      assertEquals("Doe", call.lastName)
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
