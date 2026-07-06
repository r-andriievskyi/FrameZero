package com.frame.zero.testing

import com.frame.zero.domain.User
import com.frame.zero.repository.user.UserRepository

class FakeUserRepository(
  private val user: User = User(id = "", email = "", firstName = "", lastName = ""),
  private val throws: Throwable? = null
) : UserRepository {
  var getMeCalls: Int = 0
    private set

  override suspend fun getMe(): User {
    getMeCalls++
    throws?.let { throw it }
    return user
  }
}
