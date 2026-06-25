plugins { id("crossplatform.library") }

base { archivesName = "repository-user" }

kotlin {
  android { namespace = "com.frame.zero.repository.user" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
