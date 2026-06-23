package com.frame.zero.testing

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.repository.user.UserRepository

class FakeUserRepository(
  private val userDto: UserDto = UserDto(id = "", email = "", firstName = "", lastName = ""),
  private val throws: Throwable? = null
) : UserRepository {
  var getMeCalls: Int = 0
    private set

  override suspend fun getMe(): UserDto {
    getMeCalls++
    throws?.let { throw it }
    return userDto
  }
}
