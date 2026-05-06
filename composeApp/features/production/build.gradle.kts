plugins { id("crossplatform.kmp.library.compose") }

base { archivesName = "ui-feature-production" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared.features.production)
      implementation(libs.compose.components.resources)
      implementation(projects.composeApp.shared.designSystem)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.feature.production.ui" }
