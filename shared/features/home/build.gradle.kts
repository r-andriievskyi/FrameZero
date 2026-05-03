plugins { id("crossplatform.kmp.library") }

base { archivesName = "feature-home" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(libs.decompose)
      implementation(libs.koin.core)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.feature.home" }
