plugins { id("crossplatform.kmp.library.compose") }

base { archivesName = "ui-feature-home" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared.features.home)
      implementation(projects.composeApp.shared.designSystem)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.feature.home.ui" }
