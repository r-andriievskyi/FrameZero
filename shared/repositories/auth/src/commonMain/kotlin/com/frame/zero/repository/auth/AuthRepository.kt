package com.frame.zero.repository.auth

import com.frame.zero.auth.dto.UserDto

interface AuthRepository {
  suspend fun register(
    email: String,
    password: String,
    firstName: String,
    lastName: String,
  ): UserDto

  suspend fun login(email: String, password: String): UserDto

  suspend fun logout()

  suspend fun getCurrentUser(): UserDto
}
