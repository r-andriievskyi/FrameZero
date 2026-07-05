plugins {
  id("crossplatform.library.compose")
  id("crossplatform.screenshot")
}

base { archivesName = "ui-feature-production" }

kotlin {
  android { namespace = "com.frame.zero.feature.production.ui" }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared.features.production)
      implementation(libs.compose.components.resources)
      implementation(projects.composeApp.shared.designSystem)
      implementation(projects.composeApp.shared.uiText)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
