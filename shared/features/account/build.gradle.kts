plugins {
  id("crossplatform.library")
  id("crossplatform.di")
}

base { archivesName = "feature-account" }

kotlin {
  android { namespace = "com.frame.zero.feature.account" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(libs.decompose)
      implementation(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies {
      implementation(libs.bundles.commonTest)
      implementation(libs.multiplatformSettings)
      implementation(libs.multiplatformSettings.test)
    }
  }
}
