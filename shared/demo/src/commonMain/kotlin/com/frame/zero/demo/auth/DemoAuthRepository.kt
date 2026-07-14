package com.frame.zero.demo.auth

import com.frame.zero.core.session.SessionAuthOperations
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.core.session.UserCache
import com.frame.zero.demo.DemoData
import com.frame.zero.domain.User
import com.frame.zero.repository.auth.AuthRepository

/**
 * Fake auth for demo builds: any credentials are accepted, nothing hits the network. Fake tokens
 * are stored so a relaunch stays signed in, and [SessionAuthOperations.fetchCurrentUser] serves
 * the cached identity the user typed at login.
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
  ): User = signIn(User(id = DemoData.USER_ID, email = email, firstName = firstName, lastName = lastName))

  override suspend fun login(
    email: String,
    password: String
  ): User {
    val cached = userCache.load()?.takeIf { it.email.equals(email, ignoreCase = true) }
    return signIn(
      cached ?: User(id = DemoData.USER_ID, email = email, firstName = displayNameFrom(email), lastName = "")
    )
  }

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

  private fun displayNameFrom(email: String): String =
    email.substringBefore('@')
      .substringBefore('.')
      .substringBefore('+')
      .replaceFirstChar { it.uppercase() }
      .ifBlank { DemoData.defaultUser.firstName }
}
