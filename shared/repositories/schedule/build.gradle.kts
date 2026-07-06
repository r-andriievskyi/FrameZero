plugins { id("crossplatform.library") }

base { archivesName = "repository-schedule" }

kotlin {
  android { namespace = "com.frame.zero.repository.schedule" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.dto)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
