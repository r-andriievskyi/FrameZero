plugins {
  id("crossplatform.library")
}

base { archivesName = "repository-chat" }

kotlin {
  android { namespace = "com.frame.zero.repository.chat" }

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
      implementation(projects.shared.testFixtures)
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.androidx.room.runtime)
      implementation(libs.androidx.sqlite.bundled)
    }
  }
}
