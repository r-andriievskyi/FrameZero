plugins { id("crossplatform.kmp.library.compose") }

base { archivesName = "ui-feature-account" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared.features.account)
      implementation(libs.compose.components.resources)
      implementation(projects.composeApp.shared.designSystem)
    }
  }
}

android { namespace = "com.frame.zero.feature.account.ui" }
