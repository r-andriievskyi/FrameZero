plugins {
  id("crossplatform.library")
  id("crossplatform.di")
}

base { archivesName = "feature-production-details" }

kotlin {
  android { namespace = "com.frame.zero.feature.production.details" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.uiText)
      api(libs.decompose)
      implementation(projects.shared.repositories.productions.api)
      implementation(projects.shared.repositories.tasks.api)
      implementation(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(projects.shared.database)
      implementation(libs.bundles.commonTest)
      implementation(libs.ktor.clientMock)
    }
  }
}
