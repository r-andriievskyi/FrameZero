package com.frame.zero.testing

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.repository.auth.AuthRepository

data class RegisterCall(
  val email: String,
  val password: String,
  val firstName: String,
  val lastName: String
)

class FakeAuthRepository(
  private val loginUserDto: UserDto = UserDto("", "", "", ""),
  private val loginThrows: Throwable? = null,
  private val registerUserDto: UserDto = UserDto("", "", "", ""),
  private val registerThrows: Throwable? = null,
  private val logoutThrows: Throwable? = null,
  private val currentUserDto: UserDto = UserDto("", "", "", ""),
  private val currentUserThrows: Throwable? = null
) : AuthRepository {
  val loginCalls: MutableList<Pair<String, String>> = mutableListOf()
  val registerCalls: MutableList<RegisterCall> = mutableListOf()
  var logoutCalls: Int = 0
    private set

  override suspend fun login(
    email: String,
    password: String
  ): UserDto {
    loginCalls += email to password
    loginThrows?.let { throw it }
    return loginUserDto
  }

  override suspend fun register(
    email: String,
    password: String,
    firstName: String,
    lastName: String
  ): UserDto {
    registerCalls += RegisterCall(email, password, firstName, lastName)
    registerThrows?.let { throw it }
    return registerUserDto
  }

  override suspend fun logout() {
    logoutCalls++
    logoutThrows?.let { throw it }
  }

  override suspend fun getCurrentUser(): UserDto {
    currentUserThrows?.let { throw it }
    return currentUserDto
  }
}
