package com.frame.zero.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.frame.zero.config.JwtConfig
import java.util.Date
import java.util.UUID

class JwtService(
  private val config: JwtConfig
) {
  private val algorithm = Algorithm.HMAC256(config.secret)

  val verifier: JWTVerifier = JWT.require(algorithm)
    .withIssuer(config.issuer)
    .withAudience(config.audience)
    .build()

  fun createAccessToken(
    userId: UUID,
    email: String
  ): String {
    val now = System.currentTimeMillis()
    return JWT
      .create()
      .withIssuer(config.issuer)
      .withAudience(config.audience)
      .withSubject(userId.toString())
      .withClaim(EMAIL_CLAIM, email)
      .withIssuedAt(Date(now))
      .withExpiresAt(Date(now + config.accessTokenTtl.inWholeMilliseconds))
      .sign(algorithm)
  }

  companion object {
    const val EMAIL_CLAIM = "email"
  }
}
