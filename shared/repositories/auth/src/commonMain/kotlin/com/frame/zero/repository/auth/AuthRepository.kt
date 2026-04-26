package com.frame.zero.repository.auth

interface AuthRepository {
  suspend fun authenticate(email: String, password: String): Result<Unit>
}
