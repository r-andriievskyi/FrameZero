plugins { id("crossplatform.kmp.library.compose") }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared.features.auth)
      implementation(projects.designSystem)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.feature.auth.ui" }
