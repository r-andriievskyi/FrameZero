plugins { id("crossplatform.kmp.library.compose") }

base { archivesName = "ui-feature-auth" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared.features.auth)
      implementation(libs.compose.components.resources)
      implementation(projects.composeApp.shared.designSystem)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.feature.auth.ui" }
