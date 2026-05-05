package com.frame.zero.auth

import com.auth0.jwt.exceptions.JWTVerificationException
import com.frame.zero.config.JwtConfig
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class JwtServiceTest {
  private val baseConfig =
    JwtConfig(
      secret = "test-secret-must-be-long-enough-for-hmac256",
      issuer = "test-issuer",
      audience = "test-audience",
      realm = "test-realm",
      accessTokenTtl = 15.minutes,
      refreshTokenTtl = 30.days,
    )

  @Test
  fun `verifier accepts tokens issued by the same service`() {
    val service = JwtService(baseConfig)
    val userId = UUID.randomUUID()

    val token = service.createAccessToken(userId, "u@x.com")
    val decoded = service.verifier.verify(token)

    assertEquals(userId.toString(), decoded.subject)
    assertEquals("u@x.com", decoded.getClaim(JwtService.EMAIL_CLAIM).asString())
    assertEquals("test-issuer", decoded.issuer)
    assertEquals(listOf("test-audience"), decoded.audience)
  }

  @Test
  fun `verifier rejects tokens with a different issuer`() {
    val signer = JwtService(baseConfig.copy(issuer = "other-issuer"))
    val token = signer.createAccessToken(UUID.randomUUID(), "u@x.com")

    assertFailsWith<JWTVerificationException> { JwtService(baseConfig).verifier.verify(token) }
  }

  @Test
  fun `verifier rejects tokens with a different audience`() {
    val signer = JwtService(baseConfig.copy(audience = "other-audience"))
    val token = signer.createAccessToken(UUID.randomUUID(), "u@x.com")

    assertFailsWith<JWTVerificationException> { JwtService(baseConfig).verifier.verify(token) }
  }

  @Test
  fun `verifier rejects tokens signed with a different secret`() {
    val signer = JwtService(baseConfig.copy(secret = "different-secret-also-long-enough"))
    val token = signer.createAccessToken(UUID.randomUUID(), "u@x.com")

    assertFailsWith<JWTVerificationException> { JwtService(baseConfig).verifier.verify(token) }
  }

  @Test
  fun `verifier rejects expired tokens`() {
    val service = JwtService(baseConfig.copy(accessTokenTtl = (-10).seconds))
    val token = service.createAccessToken(UUID.randomUUID(), "u@x.com")

    assertFailsWith<JWTVerificationException> { service.verifier.verify(token) }
  }

  @Test
  fun `verifier rejects tampered tokens`() {
    val service = JwtService(baseConfig)
    val token = service.createAccessToken(UUID.randomUUID(), "u@x.com")
    val tampered = token.dropLast(1) + if (token.last() == 'A') 'B' else 'A'

    assertFailsWith<JWTVerificationException> { service.verifier.verify(tampered) }
  }
}
