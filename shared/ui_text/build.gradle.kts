plugins { id("crossplatform.library") }

base { archivesName = "ui-text" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.compose.components.resources)
    }
  }
}

android { namespace = "com.frame.zero.ui_text" }
