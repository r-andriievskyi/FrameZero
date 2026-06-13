plugins { id("crossplatform.library.compose") }

base { archivesName = "feature-production-details" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.uiText)
      api(projects.shared.repositories.productions)
      api(libs.decompose)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.compose.components.resources)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.ktor.clientMock)
    }
  }
}

android { namespace = "com.frame.zero.feature.production.details" }
