plugins { id("crossplatform.library") }

base { archivesName = "repository-schedule" }

kotlin {
  android { namespace = "com.frame.zero.repository.schedule" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.domain)
      implementation(libs.bundles.koinRuntime)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
