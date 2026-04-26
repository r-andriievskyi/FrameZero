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

  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
  implementation(projects.shared)
  implementation(libs.logback)
  implementation(libs.ktor.serverCore)
  implementation(libs.ktor.serverNetty)
  implementation(libs.ktor.serverAuth)
  implementation(libs.ktor.serverAuthJwt)
  implementation(libs.ktor.serverContentNegotiation)
  implementation(libs.ktor.serverStatusPages)
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
}
