plugins { id("crossplatform.kmp.library.compose") }

base { archivesName = "ui-feature-auth" }

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
