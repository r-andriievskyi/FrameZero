plugins { id("crossplatform.library") }

base { archivesName = "repository-dashboard" }

kotlin {
  android { namespace = "com.frame.zero.repository.dashboard" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
