package com.frame.zero.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PasswordHasher {
  // bcrypt at cost 12 is ~200-300ms of CPU per call. Run it on the default
  // (CPU) dispatcher so it never blocks a Ktor request thread or a DB connection.
  suspend fun hash(password: String): String =
    withContext(Dispatchers.Default) {
      BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray())
    }

  suspend fun verify(
    password: String,
    hash: String
  ): Boolean =
    withContext(Dispatchers.Default) {
      BCrypt.verifyer().verify(password.toCharArray(), hash).verified
    }

  private companion object {
    const val BCRYPT_COST = 12
  }
}
