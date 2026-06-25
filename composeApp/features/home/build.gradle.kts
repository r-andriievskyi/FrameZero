plugins {
  id("crossplatform.library.compose")
  alias(libs.plugins.roborazzi)
}

base { archivesName = "ui-feature-home" }

kotlin {
  android { namespace = "com.frame.zero.feature.home.ui" }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared.features.home)
      implementation(libs.kotlinx.collections.immutable)
      implementation(libs.androidx.paging.compose)
      implementation(libs.compose.components.resources)
      implementation(projects.composeApp.shared.designSystem)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
    androidHostTest.dependencies {
      implementation(libs.compose.uiTestManifest)
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
