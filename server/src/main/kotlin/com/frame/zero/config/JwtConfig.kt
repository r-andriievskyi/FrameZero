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
  val refreshTokenTtl: Duration
) {
  companion object {
    private const val DEV_SECRET = "dev-secret-do-not-use-in-production"

    fun fromEnv(): JwtConfig {
      val isDev = System.getProperty("io.ktor.development")?.toBoolean() == true
      val secret = System.getenv("JWT_SECRET")?.takeIf { it.isNotBlank() }
        ?: if (isDev) {
          DEV_SECRET
        } else {
          error("JWT_SECRET environment variable is required (set io.ktor.development=true for local dev)")
        }
      return JwtConfig(
        secret = secret,
        issuer = env("JWT_ISSUER", "framezero"),
        audience = env("JWT_AUDIENCE", "framezero-api"),
        realm = env("JWT_REALM", "framezero"),
        accessTokenTtl = 15.minutes,
        refreshTokenTtl = 30.days
      )
    }
  }
}

private fun env(
  name: String,
  default: String
): String = System.getenv(name)?.takeIf { it.isNotBlank() } ?: default
