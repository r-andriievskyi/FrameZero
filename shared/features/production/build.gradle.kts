plugins {
  id("crossplatform.library")
  id("crossplatform.di")
}

base { archivesName = "feature-production" }

kotlin {
  android { namespace = "com.frame.zero.feature.production" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.uiText)
      api(libs.essenty.instanceKeeper)
      implementation(projects.shared.repositories.productions.api)
      implementation(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.bundles.commonTest)
    }
  }
}
