plugins { id("crossplatform.library.compose") }

base { archivesName = "feature-task-list" }

kotlin {
  android { namespace = "com.frame.zero.feature.task.list" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.uiText)
      api(libs.decompose)
      api(libs.androidx.paging.common)
      implementation(projects.shared.repositories.tasks.api)
      implementation(libs.bundles.koinRuntime)
      implementation(libs.compose.components.resources)
    }
    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.bundles.commonTest)
    }
  }
}
