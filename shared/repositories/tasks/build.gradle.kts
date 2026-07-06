plugins { id("crossplatform.library") }

base { archivesName = "repository-tasks" }

kotlin {
  android { namespace = "com.frame.zero.repository.tasks" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.domain)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
