plugins { id("crossplatform.library.compose") }

base { archivesName = "ui-feature-task-details" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared.features.taskDetails)
      implementation(libs.compose.components.resources)
      implementation(projects.composeApp.shared.designSystem)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.feature.task.details.ui" }
