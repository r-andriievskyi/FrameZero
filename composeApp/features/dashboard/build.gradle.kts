plugins { id("crossplatform.kmp.library.compose") }

base { archivesName = "ui-feature-dashboard" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared.features.dashboard)
      implementation(projects.designSystem)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.feature.dashboard.ui" }
