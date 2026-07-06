plugins { id("crossplatform.library.compose") }

base { archivesName = "feature-chat" }

kotlin {
  android { namespace = "com.frame.zero.feature.chat" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.uiText)
      api(libs.decompose)
      api(libs.androidx.paging.common)
      implementation(projects.shared.repositories.chat.api)
      implementation(libs.bundles.koinRuntime)
      implementation(libs.kotlinx.datetime)
      implementation(libs.compose.components.resources)
    }
    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.bundles.commonTest)
    }
  }
}
