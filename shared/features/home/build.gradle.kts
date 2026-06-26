plugins { id("crossplatform.library.compose") }

base { archivesName = "feature-home" }

kotlin {
  android { namespace = "com.frame.zero.feature.home" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(libs.decompose)
      api(libs.kotlinx.collections.immutable)
      implementation(projects.shared.repositories.user)
      implementation(projects.shared.repositories.dashboard)
      implementation(projects.shared.repositories.productions)
      implementation(projects.shared.repositories.schedule)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.ktor.clientCore)
      implementation(libs.ktor.clientContentNegotiation)
      implementation(libs.ktor.clientSerializationJson)
    }
    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.ktor.clientMock)
    }
  }
}
