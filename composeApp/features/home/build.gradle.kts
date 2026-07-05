plugins {
  id("crossplatform.library.compose")
  id("crossplatform.screenshot")
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
  }
}
