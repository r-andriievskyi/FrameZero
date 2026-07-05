plugins {
  id("crossplatform.library.compose")
  id("crossplatform.screenshot")
}

base { archivesName = "ui-feature-production-details" }

kotlin {
  android { namespace = "com.frame.zero.feature.production.details.ui" }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared.features.productionDetails)
      implementation(libs.kotlinx.collections.immutable)
      implementation(libs.compose.components.resources)
      implementation(projects.composeApp.shared.designSystem)
      implementation(projects.composeApp.shared.uiText)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
