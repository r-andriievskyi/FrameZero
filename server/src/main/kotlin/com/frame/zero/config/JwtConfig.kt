package com.frame.zero.config

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

data class JwtConfig(
  val secret: String,
  val issuer: String,
  val audience: String,
  val realm: String,
  val accessTokenTtl: Duration,
  val refreshTokenTtl: Duration,
) {
  companion object {
    fun fromEnv(): JwtConfig =
      JwtConfig(
        secret = env("JWT_SECRET", "dev-secret-do-not-use-in-production"),
        issuer = env("JWT_ISSUER", "framezero"),
        audience = env("JWT_AUDIENCE", "framezero-api"),
        realm = env("JWT_REALM", "framezero"),
        accessTokenTtl = 15.minutes,
        refreshTokenTtl = 30.days,
      )
  }
}

private fun env(
  name: String,
  default: String
): String = System.getenv(name)?.takeIf { it.isNotBlank() } ?: default
