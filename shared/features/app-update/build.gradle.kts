plugins { id("crossplatform.library.compose") }

base { archivesName = "feature-app-update" }

kotlin {
  android { namespace = "com.frame.zero.feature.app_update" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      implementation(projects.shared.repositories.appUpdate.api)
      implementation(libs.bundles.koinRuntime)
    }
    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.bundles.commonTest)
    }
  }
}
