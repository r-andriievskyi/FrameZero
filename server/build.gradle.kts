plugins {
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.ktor)
  application
  id("crossplatform.code.quality")
}

group = "com.frame.zero"

version = "1.0.0"

application {
  mainClass.set("com.frame.zero.ApplicationKt")

  val isDevelopment: Boolean = project.ext.has("development") ||
    System.getenv("KTOR_ENV") != "production"
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

tasks.named<JavaExec>("run") {
  val dotenv = rootProject.file(".env")
  if (dotenv.exists()) {
    dotenv.readLines()
      .map { it.trim() }
      .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains('=') }
      .forEach { line ->
        val (key, value) = line.split('=', limit = 2)
        val name = key.trim()
        if (System.getenv(name) == null) {
          environment(name, value.trim().trim('"', '\''))
        }
      }
  }
}

dependencies {
  // No client-module dependency by design: server owns its own copy of the wire
  // DTOs (com.frame.zero.dto.*, auth.dto.*, the wire enums, Constants) so it can be
  // lifted into a standalone repo. Keep that copy in sync with shared/ by hand.
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.datetime)
  implementation(libs.logback)
  implementation(libs.ktor.serverCore)
  implementation(libs.ktor.serverNetty)
  implementation(libs.ktor.serverAuth)
  implementation(libs.ktor.serverAuthJwt)
  implementation(libs.ktor.serverCallId)
  implementation(libs.ktor.serverCallLogging)
  implementation(libs.ktor.serverContentNegotiation)
  implementation(libs.ktor.serverCors)
  implementation(libs.ktor.serverForwardedHeader)
  implementation(libs.ktor.serverMetricsMicrometer)
  implementation(libs.ktor.serverStatusPages)
  implementation(libs.ktor.serverRateLimit)
  implementation(libs.ktor.serializationKotlinxJson)
  implementation(libs.koin.ktor)
  implementation(libs.koin.loggerSlf4j)
  implementation(libs.exposed.core)
  implementation(libs.exposed.jdbc)
  implementation(libs.exposed.kotlinDatetime)
  implementation(libs.flyway.core)
  runtimeOnly(libs.flyway.databasePostgresql)
  implementation(libs.hikariCp)
  implementation(libs.postgresql)
  implementation(libs.micrometer.registryPrometheus)
  implementation(libs.bcrypt)
  implementation(libs.firebase.admin)
  testImplementation(libs.ktor.serverTestHost)
  testImplementation(libs.kotlin.testJunit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.testcontainers.postgresql)
}

tasks.withType<Test>().configureEach {
  // The Gradle test JVM doesn't inherit the shell's Docker context, so on macOS
  // Docker Desktop Testcontainers can't locate or talk to the engine out of the
  // box. When DOCKER_HOST isn't already set, point it at the raw engine socket and
  // apply the two Docker-Desktop workarounds below. On Linux/CI DOCKER_HOST is
  // unset and the mac socket is absent, so none of this applies and Testcontainers'
  // own default detection takes over.
  if (System.getenv("DOCKER_HOST") == null) {
    val home = System.getProperty("user.home")
    val rawSocket = file("$home/Library/Containers/com.docker.docker/Data/docker.raw.sock")
    if (rawSocket.exists()) {
      environment("DOCKER_HOST", "unix://${rawSocket.absolutePath}")
      // Modern Docker Desktop rejects the docker-java default API version (1.32);
      // docker-java reads the target from the `api.version` system property.
      systemProperty("api.version", "1.44")
      // Ryuk (and other helpers) bind-mount the socket; the raw socket path can't
      // be mounted, so point the in-container mount at the standard symlink.
      environment("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", "/var/run/docker.sock")
    }
  }
}
