plugins { id("crossplatform.library.compose") }

base { archivesName = "ui-text" }

// Public: the canonical error strings are asserted against directly from other modules'
// tests (e.g. auth's ViewModel tests confirm a failure falls back to the shared copy).
compose.resources {
  publicResClass = true
}

kotlin {
  android { namespace = "com.frame.zero.ui_text" }

  sourceSets {
    commonMain.dependencies {
      api(libs.compose.components.resources)
      implementation(projects.shared.domain)
    }
  }
}
