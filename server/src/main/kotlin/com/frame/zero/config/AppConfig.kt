package com.frame.zero.config

data class AppConfig(
  val database: DatabaseConfig,
  val jwt: JwtConfig,
  val firebase: FirebaseConfig,
  val fileStorage: FileStorageConfig,
  val corsAllowedOrigins: List<String> = emptyList(),
  val isDevelopment: Boolean = false
) {
  companion object {
    fun fromEnv(): AppConfig {
      val config = AppConfig(
        database = DatabaseConfig.fromEnv(),
        jwt = JwtConfig.fromEnv(),
        firebase = FirebaseConfig.fromEnv(),
        fileStorage = FileStorageConfig.fromEnv(),
        corsAllowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS")
          .orEmpty()
          .split(',')
          .mapNotNull { it.trim().ifEmpty { null } },
        isDevelopment = System.getProperty("io.ktor.development")?.toBoolean() == true
      )
      config.validate()
      return config
    }
  }

  private fun validate() {
    require(database.url.startsWith("jdbc:")) {
      "DATABASE_URL must be a valid JDBC URL (got: '${database.url}')"
    }
    require(database.user.isNotBlank()) { "DATABASE_USER must not be blank" }
    require(firebase.credentialsPath.isNotBlank()) {
      "FIREBASE_CREDENTIALS_PATH must point to a Firebase service-account JSON " +
        "(needed to send push notifications)"
    }
    fileStorage.ensureWritableDir()
  }
}

data class FileStorageConfig(
  val directory: String
) {
  companion object {
    fun fromEnv(): FileStorageConfig =
      FileStorageConfig(
        directory = env("FILE_STORAGE_DIR", "./uploads")
      )
  }

  fun ensureWritableDir() {
    val dir = java.io.File(directory)
    require(dir.exists() || dir.mkdirs()) {
      "FILE_STORAGE_DIR '$directory' does not exist and could not be created"
    }
    require(dir.isDirectory && dir.canWrite()) {
      "FILE_STORAGE_DIR '$directory' must be a writable directory"
    }
  }
}

data class FirebaseConfig(
  val credentialsPath: String
) {
  companion object {
    fun fromEnv(): FirebaseConfig =
      FirebaseConfig(
        credentialsPath = System.getenv("FIREBASE_CREDENTIALS_PATH").orEmpty().trim()
      )
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
