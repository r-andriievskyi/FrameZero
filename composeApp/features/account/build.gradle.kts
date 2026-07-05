plugins {
  id("crossplatform.library.compose")
  id("crossplatform.screenshot")
}

base { archivesName = "ui-feature-account" }

kotlin {
  android { namespace = "com.frame.zero.feature.account.ui" }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared.features.account)
      implementation(libs.compose.components.resources)
      implementation(projects.composeApp.shared.designSystem)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
