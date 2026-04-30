plugins { id("crossplatform.kmp.library.compose") }

android { namespace = "com.frame.zero.design_system" }

compose.resources {
  packageOfResClass = "com.discovery.playground.shared.design_system.generated.resources"
  publicResClass = false
  generateResClass = always
}

kotlin {
  sourceSets {
    commonMain.dependencies { implementation(libs.compose.components.resources) }
    androidMain.dependencies { implementation(libs.androidx.core.ktx) }
  }
}
