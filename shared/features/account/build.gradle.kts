plugins { id("crossplatform.kmp.library") }

base { archivesName = "feature-account" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(libs.decompose)
      implementation(libs.koin.core)
    }
  }
}

android { namespace = "com.frame.zero.feature.account" }
