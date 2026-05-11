plugins { id("crossplatform.kmp.library.compose") }

base { archivesName = "ui-feature-production-details" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared.features.productionDetails)
      implementation(libs.compose.components.resources)
      implementation(projects.composeApp.shared.designSystem)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.feature.production.details.ui" }
