package com.frame.zero.repository.user

import com.frame.zero.domain.User

interface UserRepository {
  suspend fun getMe(): User
}
