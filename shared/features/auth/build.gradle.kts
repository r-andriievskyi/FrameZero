plugins { id("crossplatform.kmp.library.compose") }

base { archivesName = "feature-auth" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.uiText)
      api(projects.shared.repositories.auth)
      api(projects.shared.repositories.user)
      api(libs.decompose)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.ktor.clientContentNegotiation)
      implementation(libs.ktor.clientSerializationJson)
      implementation(libs.compose.components.resources)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.ktor.clientMock)
      implementation(libs.ktor.clientContentNegotiation)
      implementation(libs.ktor.clientSerializationJson)
      implementation(libs.multiplatformSettings.test)
    }
  }
}

android { namespace = "com.frame.zero.feature.auth" }
