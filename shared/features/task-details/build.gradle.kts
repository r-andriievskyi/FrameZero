plugins { id("crossplatform.library") }

base { archivesName = "feature-task-details" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(libs.decompose)
      implementation(projects.shared.repositories.tasks)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.ktor.clientCore)
      implementation(libs.ktor.clientContentNegotiation)
      implementation(libs.ktor.clientSerializationJson)
    }
    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.ktor.clientMock)
    }
  }
}

android { namespace = "com.frame.zero.feature.task.details" }
