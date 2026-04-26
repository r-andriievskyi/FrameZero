package com.frame.zero.repository.auth

import kotlinx.coroutines.delay

private const val FAKE_NETWORK_DELAY_MS = 600L

class InMemoryAuthRepository : AuthRepository {
  override suspend fun authenticate(email: String, password: String): Result<Unit> {
    delay(FAKE_NETWORK_DELAY_MS)
    return Result.success(Unit)
  }
}
