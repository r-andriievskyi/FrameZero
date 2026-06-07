plugins { id("crossplatform.library") }

base { archivesName = "repository-dashboard" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.repository.dashboard" }
