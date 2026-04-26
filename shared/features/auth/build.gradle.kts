plugins { id("crossplatform.kmp.library") }

base { archivesName = "feature-auth" }

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
