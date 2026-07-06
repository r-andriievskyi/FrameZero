plugins { id("crossplatform.library") }

base { archivesName = "repository-auth" }

kotlin {
  android { namespace = "com.frame.zero.repository.auth" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.domain)
      implementation(libs.bundles.koinRuntime)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
