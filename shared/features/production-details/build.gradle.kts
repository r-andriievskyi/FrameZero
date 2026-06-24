plugins { id("crossplatform.library.compose") }

base { archivesName = "feature-production-details" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.uiText)
      api(libs.decompose)
      implementation(projects.shared.repositories.productions)
      implementation(projects.shared.repositories.tasks)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.compose.components.resources)
    }
    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.ktor.clientMock)
    }
  }
}

android { namespace = "com.frame.zero.feature.production.details" }
