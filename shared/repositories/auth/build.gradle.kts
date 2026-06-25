plugins { id("crossplatform.library") }

base { archivesName = "repository-auth" }

kotlin {
  android { namespace = "com.frame.zero.repository.auth" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
