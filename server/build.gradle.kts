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
  implementation(libs.ktor.serverStatusPages)
  implementation(libs.ktor.serverRateLimit)
  implementation(libs.ktor.serializationKotlinxJson)
  implementation(libs.koin.ktor)
  implementation(libs.koin.loggerSlf4j)
  implementation(libs.exposed.core)
  implementation(libs.exposed.jdbc)
  implementation(libs.exposed.javaTime)
  implementation(libs.hikariCp)
  implementation(libs.postgresql)
  implementation(libs.bcrypt)
  testImplementation(libs.ktor.serverTestHost)
  testImplementation(libs.kotlin.testJunit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.h2)
}
