plugins {
  id("crossplatform.library")
  alias(libs.plugins.kotlinSerialization)
}

base { archivesName = "shared-domain" }

kotlin {
  android { namespace = "com.frame.zero.shared.domain" }

  sourceSets {
    commonMain.dependencies {
      api(libs.kotlinx.datetime)
      api(libs.kotlinx.collections.immutable)
      api(libs.kotlinx.serialization.json)
      implementation(libs.ktor.clientCore)
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.ktor.clientMock)
      implementation(libs.kotlinx.coroutines.test)
    }
  }
}
