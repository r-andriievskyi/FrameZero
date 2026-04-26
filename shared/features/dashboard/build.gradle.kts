plugins { id("crossplatform.kmp.library") }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.decompose)
      implementation(libs.koin.core)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.feature.dashboard" }
