package com.frame.zero.demo.auth

import com.frame.zero.core.session.SessionAuthOperations
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.core.session.UserCache
import com.frame.zero.demo.DemoData
import com.frame.zero.domain.User
import com.frame.zero.repository.auth.AuthRepository

/**
 * Fake auth for demo builds: any credentials are accepted, nothing hits the network. Identity is
 * always the seeded [DemoData.defaultUser] — typed email/name is ignored — so it stays consistent
 * with the curated crew/task data. Fake tokens are stored so a relaunch stays signed in.
 */
internal class DemoAuthRepository(
  private val tokenStorage: TokenStorage,
  private val userCache: UserCache
) : AuthRepository,
  SessionAuthOperations {
  override suspend fun register(
    email: String,
    password: String,
    firstName: String,
    lastName: String
  ): User = signIn(DemoData.defaultUser)

  override suspend fun login(
    email: String,
    password: String
  ): User = signIn(DemoData.defaultUser)

  override suspend fun logout() {
    // Session cleanup (tokens, cache, cleaners) is driven by SessionManager.forceLogout().
  }

  override suspend fun getCurrentUser(): User = userCache.load() ?: DemoData.defaultUser

  override suspend fun fetchCurrentUser(): User = userCache.load() ?: DemoData.defaultUser

  override suspend fun signOutRemote() = Unit

  private fun signIn(user: User): User {
    tokenStorage.saveTokens(accessToken = "demo-access-token", refreshToken = "demo-refresh-token")
    return user
  }
}
