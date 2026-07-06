plugins { id("crossplatform.library.compose") }

base { archivesName = "feature-production-details" }

kotlin {
  android { namespace = "com.frame.zero.feature.production.details" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.uiText)
      api(libs.decompose)
      implementation(projects.shared.repositories.productions.api)
      implementation(projects.shared.repositories.tasks)
      implementation(libs.bundles.koinRuntime)
      implementation(libs.compose.components.resources)
    }
    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.bundles.commonTest)
      implementation(libs.ktor.clientMock)
    }
  }
}
