package com.frame.zero.feature.auth.usecase

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.core.session.LogoutSignal
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.SessionState
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.DomainException
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.User
import com.frame.zero.feature.auth.domain.RegisterUseCase
import com.frame.zero.feature.auth.testing.FakeAuthRepository
import com.frame.zero.feature.auth.testing.NoopSessionAuthOperations
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RegisterUseCaseTest {
  private val userDto = UserDto(id = "u1", email = "new@x.com", firstName = "", lastName = "")
  private val user = User(id = "u1", email = "new@x.com")

  @Test
  fun `success transitions session to LoggedIn`() =
    runTest {
      val repo = FakeAuthRepository(registerUserDto = userDto)
      val session = makeSessionManager()

      val outcome =
        RegisterUseCase(repo, session)(
          RegisterUseCase.Params(
            email = "new@x.com",
            password = "p",
            firstName = "Jane",
            lastName = "Doe",
          )
        )

      val success = assertIs<Outcome.Success<User>>(outcome)
      assertEquals(user, success.data)
      assertEquals(SessionState.LoggedIn(user), session.state.value)
    }

  @Test
  fun `failure leaves session state untouched`() =
    runTest {
      val repo =
        FakeAuthRepository(registerThrows = DomainException(DomainError.EmailAlreadyExists))
      val session = makeSessionManager()
      val before = session.state.value

      val outcome =
        RegisterUseCase(repo, session)(
          RegisterUseCase.Params(email = "dup@x.com", password = "p", firstName = "", lastName = "")
        )

      assertIs<Outcome.Failure>(outcome)
      assertEquals(before, session.state.value)
    }

  @Test
  fun `forwards all fields to repository`() =
    runTest {
      val repo = FakeAuthRepository(registerUserDto = userDto)

      RegisterUseCase(repo, makeSessionManager())(
        RegisterUseCase.Params(
          email = "typed@x.com",
          password = "secret",
          firstName = "Jane",
          lastName = "Doe",
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
      logoutSignal = LogoutSignal(),
      scope = backgroundScope,
    )
}
