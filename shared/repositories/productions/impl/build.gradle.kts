plugins {
  id("crossplatform.library")
}

base { archivesName = "repository-productions-impl" }

kotlin {
  android { namespace = "com.frame.zero.repository.productions.impl" }

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.repositories.productions.api)
      implementation(projects.shared)
      implementation(projects.shared.database)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.ktor.clientCore)
      implementation(libs.ktor.clientContentNegotiation)
    }

    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.androidx.room.runtime)
      implementation(libs.androidx.sqlite.bundled)
    }
  }
}
