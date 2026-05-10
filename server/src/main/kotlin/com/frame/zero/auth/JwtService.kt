package com.frame.zero.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.frame.zero.config.JwtConfig
import java.time.Instant
import java.util.UUID

class JwtService(
  private val jwtConfig: JwtConfig
) {
  private val signingAlgorithm = Algorithm.HMAC256(jwtConfig.secret)

  val tokenVerifier: JWTVerifier = JWT.require(signingAlgorithm)
    .withIssuer(jwtConfig.issuer)
    .withAudience(jwtConfig.audience)
    .build()

  fun createAccessToken(
    userId: UUID,
    email: String
  ): String {
    val now = Instant.now()
    val expiresAt = now.plusMillis(jwtConfig.accessTokenTtl.inWholeMilliseconds)
    return JWT
      .create()
      .withIssuer(jwtConfig.issuer)
      .withAudience(jwtConfig.audience)
      .withSubject(userId.toString())
      .withClaim(EMAIL_CLAIM, email)
      .withIssuedAt(now)
      .withExpiresAt(expiresAt)
      .sign(signingAlgorithm)
  }

  companion object {
    const val EMAIL_CLAIM = "email"
  }
}
