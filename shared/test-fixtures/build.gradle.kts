plugins { id("crossplatform.library") }

base { archivesName = "test-fixtures" }

kotlin {
  android { namespace = "com.frame.zero.testing" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.repositories.auth)
      api(projects.shared.repositories.user)
      api(projects.shared.repositories.dashboard)
      api(projects.shared.repositories.productions.api)
      api(projects.shared.repositories.schedule)
      api(projects.shared.repositories.tasks)
      api(projects.shared.repositories.chat.api)
      api(libs.androidx.paging.common)
      api(libs.ktor.clientMock)
      implementation(libs.ktor.clientCore)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.kotlinx.datetime)
    }
  }
}
