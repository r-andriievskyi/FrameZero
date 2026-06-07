plugins { id("crossplatform.kmp.library.compose") }

android { namespace = "com.frame.zero.shared.design_system" }

compose.resources {
  packageOfResClass = "com.frame.zero.shared.design_system.generated.resources"
  publicResClass = true
  generateResClass = always
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.androidx.paging.compose)
      api(libs.kotlinx.collections.immutable)
      implementation(libs.compose.components.resources)
    }
    androidMain.dependencies { implementation(libs.androidx.core.ktx) }
  }
}
