plugins {
  id("crossplatform.library")
}

base { archivesName = "screenshot-testing" }

kotlin {
  android { namespace = "com.frame.zero.shared.screenshot" }

  sourceSets {
    // `api` so consumers pull the whole Roborazzi/Robolectric/scanner stack transitively via the
    // single `crossplatform.screenshot` project dependency instead of re-listing ten libraries.
    androidMain.dependencies { api(libs.bundles.screenshotTest) }
  }
}
