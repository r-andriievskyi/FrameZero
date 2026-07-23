plugins { id("crossplatform.library.compose") }

base { archivesName = "feature-force-update" }

kotlin {
  android { namespace = "com.frame.zero.feature.force_update" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      implementation(projects.shared.repositories.forceUpdate.api)
      implementation(libs.bundles.koinRuntime)
    }
    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.bundles.commonTest)
    }
  }
}
