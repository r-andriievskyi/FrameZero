package com.frame.zero.demo.auth

import com.frame.zero.core.session.TokenStorage
import com.frame.zero.core.session.UserCache
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DemoAuthRepositoryTest {
  private fun repo(): Triple<DemoAuthRepository, TokenStorage, UserCache> {
    val settings = MapSettings()
    val tokenStorage = TokenStorage(settings)
    val userCache = UserCache(settings)
    return Triple(DemoAuthRepository(tokenStorage, userCache), tokenStorage, userCache)
  }

  @Test
  fun login_accepts_any_credentials_and_saves_tokens() =
    runTest {
      val (repo, tokenStorage, _) = repo()
      val user = repo.login("director@studio.com", "whatever")
      assertTrue(tokenStorage.hasTokens())
      assertEquals("director@studio.com", user.email)
    }

  @Test
  fun fetch_current_user_returns_cached_identity_after_restart() =
    runTest {
      val (repo, _, userCache) = repo()
      val user = repo.register("alex@studio.com", "pw", "Alex", "Reyes")
      // SessionManager.onAuthenticated persists to the cache; simulate that here.
      userCache.save(user)
      val fetched = repo.fetchCurrentUser()
      assertEquals("alex@studio.com", fetched.email)
      assertEquals("Alex", fetched.firstName)
    }
}
