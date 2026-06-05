package com.frame.zero.feature.account

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.core.session.LogoutSignal
import com.frame.zero.core.session.SessionAuthOperations
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.SessionState
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.domain.User
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {
  private val user = User(id = "u1", email = "u@x.com", firstName = "Ada", lastName = "Lovelace")

  @Test
  fun `state reflects logged-in user name and email`() =
    runTest {
      val session = makeSession(this)
      session.onAuthenticated(user)
      val viewModel = AccountViewModel(session, StandardTestDispatcher(testScheduler))

      advanceUntilIdle()

      assertEquals("Ada Lovelace", viewModel.state.value.userName)
      assertEquals("u@x.com", viewModel.state.value.email)
    }

  @Test
  fun `blank last name trims trailing space`() =
    runTest {
      val session = makeSession(this)
      session.onAuthenticated(user.copy(lastName = ""))
      val viewModel = AccountViewModel(session, StandardTestDispatcher(testScheduler))

      advanceUntilIdle()

      assertEquals("Ada", viewModel.state.value.userName)
    }

  @Test
  fun `signOut transitions session to LoggedOut`() =
    runTest {
      val session = makeSession(this)
      session.onAuthenticated(user)
      val viewModel = AccountViewModel(session, StandardTestDispatcher(testScheduler))
      advanceUntilIdle()

      viewModel.signOut()
      advanceUntilIdle()

      assertEquals(SessionState.LoggedOut, session.state.value)
    }

  private fun makeSession(scope: TestScope): SessionManager =
    SessionManager(
      tokenStorage = TokenStorage(MapSettings()),
      authOperations = FakeAuthOps,
      logoutSignal = LogoutSignal(),
      scope = scope.backgroundScope
    )

  private object FakeAuthOps : SessionAuthOperations {
    override suspend fun fetchCurrentUser(): UserDto = UserDto(id = "", email = "", firstName = "", lastName = "")

    override suspend fun signOutRemote() = Unit
  }
}
