plugins { id("crossplatform.library") }

base { archivesName = "repository-device-token" }

kotlin {
  android { namespace = "com.frame.zero.repository.device_token" }

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      implementation(libs.bundles.koinRuntime)
      implementation(libs.bundles.ktorClient)
    }

    commonTest.dependencies {
      implementation(libs.bundles.commonTest)
      implementation(libs.multiplatformSettings)
      implementation(libs.multiplatformSettings.test)
      implementation(libs.bundles.ktorClientTest)
      implementation(libs.kotlinx.serialization.json)
    }
  }
}
