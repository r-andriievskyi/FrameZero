plugins { id("crossplatform.kmp.library.compose") }

base { archivesName = "ui-feature-home" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared.features.home)
      implementation(libs.androidx.paging.compose)
      implementation(libs.compose.components.resources)
      implementation(projects.composeApp.shared.designSystem)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
    androidInstrumentedTest.dependencies {
      implementation(libs.androidx.testExt.junit)
      implementation(libs.compose.uiTestJUnit4)
    }
  }
}

android {
  namespace = "com.frame.zero.feature.home.ui"
  defaultConfig { testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
}

dependencies { debugImplementation(libs.compose.uiTestManifest) }
