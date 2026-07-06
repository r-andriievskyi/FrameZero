plugins { id("crossplatform.library.compose") }

base { archivesName = "feature-production" }

kotlin {
  android { namespace = "com.frame.zero.feature.production" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.uiText)
      api(libs.decompose)
      implementation(projects.shared.repositories.productions.api)
      implementation(libs.bundles.koinRuntime)
      implementation(libs.compose.components.resources)
    }
    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.bundles.commonTest)
    }
  }
}
