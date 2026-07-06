package com.frame.zero.testing

import com.frame.zero.domain.User
import com.frame.zero.repository.auth.AuthRepository

data class RegisterCall(
  val email: String,
  val password: String,
  val firstName: String,
  val lastName: String
)

class FakeAuthRepository(
  private val loginUser: User = User("", "", "", ""),
  private val loginThrows: Throwable? = null,
  private val registerUser: User = User("", "", "", ""),
  private val registerThrows: Throwable? = null,
  private val logoutThrows: Throwable? = null,
  private val currentUser: User = User("", "", "", ""),
  private val currentUserThrows: Throwable? = null
) : AuthRepository {
  val loginCalls: MutableList<Pair<String, String>> = mutableListOf()
  val registerCalls: MutableList<RegisterCall> = mutableListOf()
  var logoutCalls: Int = 0
    private set

  override suspend fun login(
    email: String,
    password: String
  ): User {
    loginCalls += email to password
    loginThrows?.let { throw it }
    return loginUser
  }

  override suspend fun register(
    email: String,
    password: String,
    firstName: String,
    lastName: String
  ): User {
    registerCalls += RegisterCall(email, password, firstName, lastName)
    registerThrows?.let { throw it }
    return registerUser
  }

  override suspend fun logout() {
    logoutCalls++
    logoutThrows?.let { throw it }
  }

  override suspend fun getCurrentUser(): User {
    currentUserThrows?.let { throw it }
    return currentUser
  }
}
