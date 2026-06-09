plugins {
  id("crossplatform.library.compose")
  alias(libs.plugins.roborazzi)
}

base { archivesName = "ui-feature-auth" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared.features.auth)
      implementation(libs.compose.components.resources)
      implementation(libs.decompose.extensionsCompose)
      implementation(projects.composeApp.shared.designSystem)
      implementation(projects.composeApp.shared.uiText)
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
  namespace = "com.frame.zero.feature.auth.ui"
  testOptions { unitTests.isIncludeAndroidResources = true }
}

dependencies { debugImplementation(libs.compose.uiTestManifest) }
