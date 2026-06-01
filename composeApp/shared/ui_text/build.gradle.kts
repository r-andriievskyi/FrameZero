plugins { id("crossplatform.kmp.library.compose") }

base { archivesName = "ui-text-compose" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared.uiText)
      implementation(libs.compose.components.resources)
    }
  }
}

android { namespace = "com.frame.zero.ui_text.compose" }
