plugins { id("crossplatform.library") }

base { archivesName = "repository-tasks-impl" }

kotlin {
  android { namespace = "com.frame.zero.repository.tasks.impl" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.repositories.tasks.api)
      implementation(projects.shared)
      implementation(projects.shared.database)
      implementation(libs.bundles.koinRuntime)
      implementation(libs.bundles.ktorClient)
    }

    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.bundles.commonTest)
      implementation(libs.ktor.clientMock)
    }
  }
}
