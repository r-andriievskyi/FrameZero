plugins { id("crossplatform.library") }

base { archivesName = "repository-dashboard" }

kotlin {
  android { namespace = "com.frame.zero.repository.dashboard" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.domain)
      implementation(libs.bundles.koinRuntime)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
