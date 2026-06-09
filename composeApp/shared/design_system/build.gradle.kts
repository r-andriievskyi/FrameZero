plugins {
  id("crossplatform.library.compose")
  alias(libs.plugins.roborazzi)
}

android {
  namespace = "com.frame.zero.shared.design_system"
  testOptions { unitTests.isIncludeAndroidResources = true }
}

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
    commonTest.dependencies { implementation(libs.kotlin.test) }
    androidUnitTest.dependencies {
      implementation(libs.kotlin.testJunit)
      implementation(libs.junit)
      implementation(libs.robolectric)
      implementation(libs.compose.uiTestJUnit4)
      implementation(libs.roborazzi)
      implementation(libs.roborazzi.compose)
      implementation(libs.roborazzi.junitRule)
      implementation(libs.roborazzi.composePreviewScannerSupport)
      implementation(libs.composablePreviewScanner.android)
    }
  }
}

dependencies { debugImplementation(libs.compose.uiTestManifest) }
