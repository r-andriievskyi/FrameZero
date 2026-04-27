package com.frame.zero.feature.auth.testing

import com.frame.zero.core.session.SessionAuthOperations
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.User
import com.frame.zero.repository.auth.AuthRepository

internal class FakeAuthRepository(
  private val loginResult: Outcome<User> = Outcome.Failure(DomainError.Unknown(null)),
  private val registerResult: Outcome<User> = Outcome.Failure(DomainError.Unknown(null)),
  private val logoutResult: Outcome<Unit> = Outcome.Success(Unit),
  private val currentUserResult: Outcome<User> = Outcome.Failure(DomainError.Unknown(null)),
) : AuthRepository {
  val loginCalls: MutableList<Pair<String, String>> = mutableListOf()
  val registerCalls: MutableList<Pair<String, String>> = mutableListOf()
  var logoutCalls: Int = 0
    private set

  override suspend fun login(email: String, password: String): Outcome<User> {
    loginCalls += email to password
    return loginResult
  }

  override suspend fun register(email: String, password: String): Outcome<User> {
    registerCalls += email to password
    return registerResult
  }

  override suspend fun logout(): Outcome<Unit> {
    logoutCalls++
    return logoutResult
  }

  override suspend fun getCurrentUser(): Outcome<User> = currentUserResult
}

internal object NoopSessionAuthOperations : SessionAuthOperations {
  override suspend fun fetchCurrentUser(): Outcome<User> =
    Outcome.Failure(DomainError.Unknown(null))

  override suspend fun signOutRemote(): Outcome<Unit> = Outcome.Success(Unit)
}
