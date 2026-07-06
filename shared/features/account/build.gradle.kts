plugins { id("crossplatform.library.compose") }

base { archivesName = "feature-account" }

kotlin {
  android { namespace = "com.frame.zero.feature.account" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(libs.decompose)
      implementation(libs.bundles.koinRuntime)
    }
    commonTest.dependencies {
      implementation(libs.bundles.commonTest)
      implementation(libs.multiplatformSettings)
      implementation(libs.multiplatformSettings.test)
    }
  }
}
