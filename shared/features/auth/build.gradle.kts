plugins { id("crossplatform.kmp.library") }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared.repositories.auth)
      api(libs.decompose)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.feature.auth" }
