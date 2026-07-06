package com.frame.zero.repository.auth

import com.frame.zero.domain.User

interface AuthRepository {
  suspend fun register(
    email: String,
    password: String,
    firstName: String,
    lastName: String
  ): User

  suspend fun login(
    email: String,
    password: String
  ): User

  suspend fun logout()

  suspend fun getCurrentUser(): User
}
