plugins { id("crossplatform.library") }

base { archivesName = "ui-text" }

kotlin {
  android { namespace = "com.frame.zero.ui_text" }

  sourceSets {
    commonMain.dependencies {
      api(libs.compose.components.resources)
    }
  }
}
