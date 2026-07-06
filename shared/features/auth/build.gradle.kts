plugins { id("crossplatform.library.compose") }

base { archivesName = "feature-auth" }

kotlin {
  android { namespace = "com.frame.zero.feature.auth" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.uiText)
      api(libs.decompose)
      implementation(projects.shared.repositories.auth)
      implementation(projects.shared.repositories.user)
      implementation(libs.bundles.koinRuntime)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.bundles.ktorClient)
      implementation(libs.compose.components.resources)
    }
    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.bundles.commonTest)
      implementation(libs.bundles.ktorClientTest)
      implementation(libs.multiplatformSettings)
      implementation(libs.multiplatformSettings.test)
    }
  }
}
