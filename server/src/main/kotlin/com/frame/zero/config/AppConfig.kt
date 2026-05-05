package com.frame.zero.config

data class AppConfig(
  val database: DatabaseConfig,
  val jwt: JwtConfig
) {
  companion object {
    fun fromEnv(): AppConfig = AppConfig(database = DatabaseConfig.fromEnv(), jwt = JwtConfig.fromEnv())
  }
}

data class DatabaseConfig(
  val url: String,
  val user: String,
  val password: String
) {
  companion object {
    fun fromEnv(): DatabaseConfig =
      DatabaseConfig(
        url = env("DATABASE_URL", "jdbc:postgresql://localhost:5432/framezero"),
        user = env("DATABASE_USER", "framezero"),
        password = env("DATABASE_PASSWORD", "framezero")
      )
  }
}

private fun env(
  name: String,
  default: String
): String = System.getenv(name)?.takeIf { it.isNotBlank() } ?: default
