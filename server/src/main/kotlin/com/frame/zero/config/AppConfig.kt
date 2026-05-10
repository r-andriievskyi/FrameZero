package com.frame.zero.config

data class AppConfig(
  val database: DatabaseConfig,
  val jwt: JwtConfig
) {
  companion object {
    fun fromEnv(): AppConfig {
      val config = AppConfig(database = DatabaseConfig.fromEnv(), jwt = JwtConfig.fromEnv())
      config.validate()
      return config
    }
  }

  private fun validate() {
    require(database.url.startsWith("jdbc:")) {
      "DATABASE_URL must be a valid JDBC URL (got: '${database.url}')"
    }
    require(database.user.isNotBlank()) { "DATABASE_USER must not be blank" }
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
