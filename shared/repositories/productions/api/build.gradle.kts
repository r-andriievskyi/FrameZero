plugins {
  id("crossplatform.library")
}

base { archivesName = "repository-productions-api" }

kotlin {
  android { namespace = "com.frame.zero.repository.productions.api" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.domain)
      api(libs.androidx.paging.common)
      api(libs.kotlinx.coroutines.core)
    }
  }
}
