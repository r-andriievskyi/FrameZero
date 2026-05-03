package com.frame.zero.core.session

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.domain.User
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

  private val userDto = UserDto(id = "u1", email = "u@x.com", firstName = "", lastName = "")
  private val user = User(id = "u1", email = "u@x.com")

  @Test
  fun `initialize transitions to LoggedOut when no tokens stored`() = runTest {
    val manager = makeManager()

    manager.initialize()

    assertEquals(SessionState.LoggedOut, manager.state.value)
  }

  @Test
  fun `initialize transitions to LoggedIn when tokens valid and fetch succeeds`() = runTest {
    val storage = TokenStorage(MapSettings()).also { it.saveTokens("a", "r") }
    val ops = FakeAuthOps(currentUserDto = userDto)
    val manager = SessionManager(storage, ops, LogoutSignal(), backgroundScope)

    manager.initialize()

    assertEquals(SessionState.LoggedIn(user), manager.state.value)
    assertEquals(1, ops.fetchCalls)
  }

  @Test
  fun `initialize forces logout when fetch throws`() = runTest {
    val storage = TokenStorage(MapSettings()).also { it.saveTokens("a", "r") }
    val ops = FakeAuthOps(fetchThrows = true)
    val manager = SessionManager(storage, ops, LogoutSignal(), backgroundScope)

    manager.initialize()

    assertEquals(SessionState.LoggedOut, manager.state.value)
    assertFalse(storage.hasTokens())
  }

  @Test
  fun `onAuthenticated transitions state to LoggedIn`() = runTest {
    val manager = makeManager()

    manager.onAuthenticated(user)

    assertEquals(SessionState.LoggedIn(user), manager.state.value)
  }

  @Test
  fun `logout invokes signOutRemote and transitions to LoggedOut`() = runTest {
    val storage = TokenStorage(MapSettings()).also { it.saveTokens("a", "r") }
    val ops = FakeAuthOps()
    val manager = SessionManager(storage, ops, LogoutSignal(), backgroundScope)
    manager.onAuthenticated(user)

    manager.logout()

    assertEquals(SessionState.LoggedOut, manager.state.value)
    assertEquals(1, ops.signOutCalls)
    assertFalse(storage.hasTokens())
  }

  @Test
  fun `logout still completes when signOutRemote throws`() = runTest {
    val storage = TokenStorage(MapSettings()).also { it.saveTokens("a", "r") }
    val ops = FakeAuthOps(signOutThrows = true)
    val manager = SessionManager(storage, ops, LogoutSignal(), backgroundScope)
    manager.onAuthenticated(user)

    manager.logout()

    assertEquals(SessionState.LoggedOut, manager.state.value)
    assertFalse(storage.hasTokens())
  }

  @Test
  fun `LogoutSignal emission forces logout`() =
    runTest(UnconfinedTestDispatcher()) {
      val storage = TokenStorage(MapSettings()).also { it.saveTokens("a", "r") }
      val ops = FakeAuthOps()
      val signal = LogoutSignal()
      val manager = SessionManager(storage, ops, signal, backgroundScope)
      manager.onAuthenticated(user)

      signal.emit()

      assertEquals(SessionState.LoggedOut, manager.state.value)
      assertFalse(storage.hasTokens())
    }

  private fun TestScope.makeManager(): SessionManager =
    SessionManager(
      tokenStorage = TokenStorage(MapSettings()),
      authOperations = FakeAuthOps(),
      logoutSignal = LogoutSignal(),
      scope = backgroundScope,
    )

  private class FakeAuthOps(
    private val currentUserDto: UserDto = UserDto("", "", "", ""),
    private val fetchThrows: Boolean = false,
    private val signOutThrows: Boolean = false,
  ) : SessionAuthOperations {
    var fetchCalls = 0
    var signOutCalls = 0

    override suspend fun fetchCurrentUser(): UserDto {
      fetchCalls++
      if (fetchThrows) throw RuntimeException("fetch failed")
      return currentUserDto
    }

    override suspend fun signOutRemote() {
      signOutCalls++
      if (signOutThrows) throw RuntimeException("network down")
    }
  }
}
