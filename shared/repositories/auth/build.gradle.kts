plugins { id("crossplatform.kmp.library") }

base { archivesName = "repository-auth" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.repository.auth" }
