plugins { id("crossplatform.kmp.library") }

base { archivesName = "feature-dashboard" }

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
