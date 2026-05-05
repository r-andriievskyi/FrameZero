package com.frame.zero.auth

import java.security.MessageDigest
import java.security.SecureRandom

class TokenHasher {
  private val random = SecureRandom()

  fun generateOpaqueToken(): String {
    val bytes = ByteArray(TOKEN_BYTE_LENGTH)
    random.nextBytes(bytes)
    return base64UrlEncode(bytes)
  }

  fun sha256(token: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(token.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
  }

  private fun base64UrlEncode(bytes: ByteArray): String =
    java.util.Base64
      .getUrlEncoder()
      .withoutPadding()
      .encodeToString(bytes)

  private companion object {
    const val TOKEN_BYTE_LENGTH = 48
  }
}
