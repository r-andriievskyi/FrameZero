plugins {
  id("crossplatform.library.compose")
  id("crossplatform.screenshot")
}

base { archivesName = "ui-feature-auth" }

kotlin {
  android { namespace = "com.frame.zero.feature.auth.ui" }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared.features.auth)
      implementation(libs.compose.components.resources)
      implementation(libs.decompose.extensionsCompose)
      implementation(projects.composeApp.shared.designSystem)
      implementation(projects.composeApp.shared.uiText)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
