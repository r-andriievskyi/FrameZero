plugins { id("crossplatform.kmp.library.compose") }

base { archivesName = "ui-feature-auth" }

kotlin {
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

android { namespace = "com.frame.zero.feature.auth.ui" }
