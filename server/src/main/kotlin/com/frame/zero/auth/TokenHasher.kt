package com.frame.zero.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

class TokenHasher {
  private val random = SecureRandom()

  fun generateOpaqueToken(): String {
    val bytes = ByteArray(TOKEN_BYTE_LENGTH)
    random.nextBytes(bytes)
    return base64UrlEncode(bytes)
  }

  @OptIn(ExperimentalStdlibApi::class)
  fun sha256(token: String): String =
    MessageDigest.getInstance("SHA-256")
      .digest(token.toByteArray(Charsets.UTF_8))
      .toHexString()

  private fun base64UrlEncode(bytes: ByteArray): String =
    Base64.getUrlEncoder()
      .withoutPadding()
      .encodeToString(bytes)

  private companion object {
    const val TOKEN_BYTE_LENGTH = 48
  }
}
