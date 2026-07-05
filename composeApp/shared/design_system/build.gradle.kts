plugins {
  id("crossplatform.library.compose")
  id("crossplatform.screenshot")
}

compose.resources {
  packageOfResClass = "com.frame.zero.shared.design_system.generated.resources"
  publicResClass = true
  generateResClass = always
}

kotlin {
  android { namespace = "com.frame.zero.shared.design_system" }

  sourceSets {
    commonMain.dependencies {
      api(libs.androidx.paging.compose)
      api(libs.kotlinx.collections.immutable)
      implementation(libs.compose.components.resources)
    }
    androidMain.dependencies { implementation(libs.androidx.core.ktx) }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
