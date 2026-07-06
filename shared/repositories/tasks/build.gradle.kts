plugins { id("crossplatform.library") }

base { archivesName = "repository-tasks" }

kotlin {
  android { namespace = "com.frame.zero.repository.tasks" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.domain)
      implementation(libs.bundles.koinRuntime)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
