package com.frame.zero.feature.auth.usecase

import com.frame.zero.core.session.LogoutSignal
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.SessionState
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.User
import com.frame.zero.feature.auth.testing.FakeAuthRepository
import com.frame.zero.feature.auth.testing.NoopSessionAuthOperations
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

class RegisterUseCaseTest {

  private val user = User(id = "u1", email = "new@x.com")

  @Test
  fun `success transitions session to LoggedIn`() = runTest {
    val repo = FakeAuthRepository(registerResult = Outcome.Success(user))
    val session = makeSessionManager()

    val outcome = RegisterUseCase(repo, session).invoke(email = "new@x.com", password = "p")

    val success = assertIs<Outcome.Success<User>>(outcome)
    assertEquals(user, success.data)
    assertEquals(SessionState.LoggedIn(user), session.state.value)
  }

  @Test
  fun `failure leaves session state untouched`() = runTest {
    val repo = FakeAuthRepository(registerResult = Outcome.Failure(DomainError.EmailAlreadyExists))
    val session = makeSessionManager()
    val before = session.state.value

    val outcome = RegisterUseCase(repo, session).invoke(email = "dup@x.com", password = "p")

    assertIs<Outcome.Failure>(outcome)
    assertEquals(before, session.state.value)
  }

  @Test
  fun `forwards email and password to repository`() = runTest {
    val repo = FakeAuthRepository(registerResult = Outcome.Success(user))

    RegisterUseCase(repo, makeSessionManager()).invoke(email = "typed@x.com", password = "secret")

    assertEquals(listOf("typed@x.com" to "secret"), repo.registerCalls)
  }

  private fun TestScope.makeSessionManager(): SessionManager =
    SessionManager(
      tokenStorage = TokenStorage(MapSettings()),
      authOperations = NoopSessionAuthOperations,
      logoutSignal = LogoutSignal(),
      scope = backgroundScope,
    )
}
