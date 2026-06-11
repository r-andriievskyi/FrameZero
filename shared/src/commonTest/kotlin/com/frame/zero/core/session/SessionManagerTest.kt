package com.frame.zero.core.session

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.domain.User
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {
  private val userDto = UserDto(id = "u1", email = "u@x.com", firstName = "", lastName = "")
  private val user = User(id = "u1", email = "u@x.com")

  @Test
  fun `initialize transitions to LoggedOut when no tokens stored`() =
    runTest {
      val manager = makeManager()

      manager.initialize()

      assertEquals(SessionState.LoggedOut, manager.state.value)
    }

  @Test
  fun `initialize transitions to LoggedIn when tokens valid and fetch succeeds`() =
    runTest {
      val storage = TokenStorage(MapSettings()).also { it.saveTokens("a", "r") }
      val ops = FakeAuthOps(currentUserDto = userDto)
      val manager = SessionManager(storage, ops, UserCache(MapSettings()), LogoutSignal(), scope = backgroundScope)

      manager.initialize()

      assertEquals(SessionState.LoggedIn(user), manager.state.value)
      assertEquals(1, ops.fetchCalls)
    }

  @Test
  fun `initialize caches the fetched user`() =
    runTest {
      val storage = TokenStorage(MapSettings()).also { it.saveTokens("a", "r") }
      val cache = UserCache(MapSettings())
      val manager = SessionManager(
        storage,
        FakeAuthOps(currentUserDto = userDto),
        cache,
        LogoutSignal(),
        scope = backgroundScope
      )

      manager.initialize()

      assertEquals(user, cache.load())
    }

  @Test
  fun `initialize restores cached user when fetch fails but tokens remain`() =
    runTest {
      val storage = TokenStorage(MapSettings()).also { it.saveTokens("a", "r") }
      val cache = UserCache(MapSettings()).also { it.save(user) }
      val manager = SessionManager(
        storage, FakeAuthOps(fetchThrows = true), cache, LogoutSignal(), scope = backgroundScope
      )

      manager.initialize()

      assertEquals(SessionState.LoggedIn(user), manager.state.value)
      assertTrue(storage.hasTokens())
    }

  @Test
  fun `initialize forces logout when fetch failure also cleared the tokens`() =
    runTest {
      val storage = TokenStorage(MapSettings()).also { it.saveTokens("a", "r") }
      val cache = UserCache(MapSettings()).also { it.save(user) }
      val ops = object : SessionAuthOperations {
        override suspend fun fetchCurrentUser(): UserDto {
          // The auth plugin clears the tokens when a 401 cannot be recovered
          // by a refresh; simulate that before failing.
          storage.clearTokens()
          throw RuntimeException("unauthorized")
        }

        override suspend fun signOutRemote() = Unit
      }
      val manager = SessionManager(storage, ops, cache, LogoutSignal(), scope = backgroundScope)

      manager.initialize()

      assertEquals(SessionState.LoggedOut, manager.state.value)
      assertNull(cache.load())
    }

  @Test
  fun `initialize forces logout when fetch throws and no user is cached`() =
    runTest {
      val storage = TokenStorage(MapSettings()).also { it.saveTokens("a", "r") }
      val ops = FakeAuthOps(fetchThrows = true)
      val manager = SessionManager(storage, ops, UserCache(MapSettings()), LogoutSignal(), scope = backgroundScope)

      manager.initialize()

      assertEquals(SessionState.LoggedOut, manager.state.value)
      assertFalse(storage.hasTokens())
    }

  @Test
  fun `onAuthenticated transitions state to LoggedIn and caches the user`() =
    runTest {
      val cache = UserCache(MapSettings())
      val manager = SessionManager(
        tokenStorage = TokenStorage(MapSettings()),
        authOperations = FakeAuthOps(),
        userCache = cache,
        logoutSignal = LogoutSignal(),
        scope = backgroundScope
      )

      manager.onAuthenticated(user)

      assertEquals(SessionState.LoggedIn(user), manager.state.value)
      assertEquals(user, cache.load())
    }

  @Test
  fun `logout invokes signOutRemote and transitions to LoggedOut`() =
    runTest {
      val storage = TokenStorage(MapSettings()).also { it.saveTokens("a", "r") }
      val ops = FakeAuthOps()
      val manager = SessionManager(storage, ops, UserCache(MapSettings()), LogoutSignal(), scope = backgroundScope)
      manager.onAuthenticated(user)

      manager.logout()

      assertEquals(SessionState.LoggedOut, manager.state.value)
      assertEquals(1, ops.signOutCalls)
      assertFalse(storage.hasTokens())
    }

  @Test
  fun `logout clears the cached user`() =
    runTest {
      val storage = TokenStorage(MapSettings()).also { it.saveTokens("a", "r") }
      val cache = UserCache(MapSettings())
      val manager = SessionManager(storage, FakeAuthOps(), cache, LogoutSignal(), scope = backgroundScope)
      manager.onAuthenticated(user)

      manager.logout()

      assertNull(cache.load())
    }

  @Test
  fun `logout still completes when signOutRemote throws`() =
    runTest {
      val storage = TokenStorage(MapSettings()).also { it.saveTokens("a", "r") }
      val ops = FakeAuthOps(signOutThrows = true)
      val manager = SessionManager(storage, ops, UserCache(MapSettings()), LogoutSignal(), scope = backgroundScope)
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
      val manager = SessionManager(storage, ops, UserCache(MapSettings()), signal, scope = backgroundScope)
      manager.onAuthenticated(user)

      signal.emit()

      assertEquals(SessionState.LoggedOut, manager.state.value)
      assertFalse(storage.hasTokens())
    }

  @Test
  fun `logout invokes all registered cleaners`() =
    runTest {
      val storage = TokenStorage(MapSettings()).also { it.saveTokens("a", "r") }
      val ops = FakeAuthOps()
      val cleanerA = FakeCleaner()
      val cleanerB = FakeCleaner()
      val manager = SessionManager(
        storage,
        ops,
        UserCache(MapSettings()),
        LogoutSignal(),
        cleaners = listOf(cleanerA, cleanerB),
        scope = backgroundScope
      )
      manager.onAuthenticated(user)

      manager.logout()

      assertEquals(1, cleanerA.clearCalls)
      assertEquals(1, cleanerB.clearCalls)
      assertEquals(SessionState.LoggedOut, manager.state.value)
    }

  @Test
  fun `LogoutSignal emission invokes cleaners`() =
    runTest(UnconfinedTestDispatcher()) {
      val storage = TokenStorage(MapSettings()).also { it.saveTokens("a", "r") }
      val cleaner = FakeCleaner()
      val signal = LogoutSignal()
      val manager = SessionManager(
        storage,
        FakeAuthOps(),
        UserCache(MapSettings()),
        signal,
        cleaners = listOf(cleaner),
        scope = backgroundScope
      )
      manager.onAuthenticated(user)

      signal.emit()

      assertEquals(1, cleaner.clearCalls)
      assertEquals(SessionState.LoggedOut, manager.state.value)
    }

  @Test
  fun `failing cleaner still clears tokens and transitions to LoggedOut`() =
    runTest {
      val storage = TokenStorage(MapSettings()).also { it.saveTokens("a", "r") }
      val cleaner = FakeCleaner(throws = true)
      val manager = SessionManager(
        storage,
        FakeAuthOps(),
        UserCache(MapSettings()),
        LogoutSignal(),
        cleaners = listOf(cleaner),
        scope = backgroundScope
      )
      manager.onAuthenticated(user)

      manager.logout()

      assertEquals(1, cleaner.clearCalls)
      assertEquals(SessionState.LoggedOut, manager.state.value)
      assertFalse(storage.hasTokens())
    }

  private fun TestScope.makeManager(): SessionManager =
    SessionManager(
      tokenStorage = TokenStorage(MapSettings()),
      authOperations = FakeAuthOps(),
      userCache = UserCache(MapSettings()),
      logoutSignal = LogoutSignal(),
      scope = backgroundScope
    )

  private class FakeAuthOps(
    private val currentUserDto: UserDto = UserDto("", "", "", ""),
    private val fetchThrows: Boolean = false,
    private val signOutThrows: Boolean = false
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

  private class FakeCleaner(
    private val throws: Boolean = false
  ) : SessionCleaner {
    var clearCalls = 0

    override suspend fun clear() {
      clearCalls++
      if (throws) throw RuntimeException("cleaner failed")
    }
  }
}
