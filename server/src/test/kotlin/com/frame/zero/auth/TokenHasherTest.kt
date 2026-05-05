package com.frame.zero.auth

import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TokenHasherTest {
  private val hasher = TokenHasher()

  @Test
  fun `sha256 matches the known vector for abc`() {
    assertEquals(
      "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
      hasher.sha256("abc"),
    )
  }

  @Test
  fun `sha256 of empty string matches the known vector`() {
    assertEquals(
      "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
      hasher.sha256(""),
    )
  }

  @Test
  fun `sha256 is deterministic`() {
    assertEquals(hasher.sha256("token-1"), hasher.sha256("token-1"))
  }

  @Test
  fun `sha256 distinguishes between distinct inputs`() {
    assertNotEquals(hasher.sha256("a"), hasher.sha256("b"))
  }

  @Test
  fun `generateOpaqueToken contains only base64url-safe characters`() {
    val token = hasher.generateOpaqueToken()

    assertTrue(token.matches(Regex("^[A-Za-z0-9_-]+$")), "produced unsafe token: $token")
  }

  @Test
  fun `generateOpaqueToken decodes to 48 bytes of randomness`() {
    val token = hasher.generateOpaqueToken()

    val decoded = Base64.getUrlDecoder().decode(token)
    assertEquals(48, decoded.size)
  }

  @Test
  fun `generateOpaqueToken yields unique tokens across many calls`() {
    val tokens = (1..100).map { hasher.generateOpaqueToken() }.toSet()

    assertEquals(100, tokens.size)
  }
}
