package com.frame.zero.auth

import at.favre.lib.crypto.bcrypt.BCrypt

class PasswordHasher {
  fun hash(password: String): String =
    BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray())

  fun verify(password: String, hash: String): Boolean =
    BCrypt.verifyer().verify(password.toCharArray(), hash).verified

  private companion object {
    const val BCRYPT_COST = 12
  }
}
