plugins { id("crossplatform.library.compose") }

base { archivesName = "feature-task-create" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.uiText)
      api(projects.shared.repositories.tasks)
      api(projects.shared.repositories.productions)
      api(libs.decompose)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.kotlinx.datetime)
      implementation(libs.compose.components.resources)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
    }
  }
}

android { namespace = "com.frame.zero.feature.task.create" }
