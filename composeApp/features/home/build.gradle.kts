plugins {
  id("crossplatform.library.compose")
  alias(libs.plugins.roborazzi)
}

base { archivesName = "ui-feature-home" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared.features.home)
      implementation(libs.kotlinx.collections.immutable)
      implementation(libs.androidx.paging.compose)
      implementation(libs.compose.components.resources)
      implementation(projects.composeApp.shared.designSystem)
    }
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

android {
  namespace = "com.frame.zero.feature.home.ui"
  testOptions { unitTests.isIncludeAndroidResources = true }
}

dependencies { debugImplementation(libs.compose.uiTestManifest) }
