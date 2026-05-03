package com.frame.zero.repository.user

import com.frame.zero.auth.dto.UserDto

interface UserRepository {
  suspend fun getMe(): UserDto
}
