plugins {
  id("crossplatform.library")
}

base { archivesName = "repository-productions" }

kotlin {
  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.database)
      api(libs.androidx.paging.common)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.ktor.clientCore)
      implementation(libs.ktor.clientContentNegotiation)
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
    }
  }
}

android { namespace = "com.frame.zero.repository.productions" }
