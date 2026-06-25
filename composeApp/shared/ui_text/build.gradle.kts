plugins { id("crossplatform.library.compose") }

base { archivesName = "ui-text-compose" }

kotlin {
  android { namespace = "com.frame.zero.ui_text.compose" }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared.uiText)
      implementation(libs.compose.components.resources)
    }
  }
}
