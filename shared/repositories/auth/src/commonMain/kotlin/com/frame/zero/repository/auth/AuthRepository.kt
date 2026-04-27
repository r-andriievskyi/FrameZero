package com.frame.zero.repository.auth

import com.frame.zero.domain.Outcome
import com.frame.zero.domain.User

interface AuthRepository {
  suspend fun register(email: String, password: String): Outcome<User>

  suspend fun login(email: String, password: String): Outcome<User>

  suspend fun logout(): Outcome<Unit>

  suspend fun getCurrentUser(): Outcome<User>
}
