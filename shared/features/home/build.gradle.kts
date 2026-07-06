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
      implementation(projects.shared.repositories.productions.api)
      implementation(projects.shared.repositories.schedule)
      implementation(libs.bundles.koinRuntime)
      implementation(libs.bundles.ktorClient)
    }
    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.bundles.commonTest)
      implementation(libs.ktor.clientMock)
    }
  }
}
