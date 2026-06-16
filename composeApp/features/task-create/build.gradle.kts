plugins { id("crossplatform.library.compose") }

base { archivesName = "ui-feature-task-create" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared.features.taskCreate)
      implementation(libs.compose.components.resources)
      implementation(projects.composeApp.shared.designSystem)
      implementation(projects.composeApp.shared.uiText)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.feature.task.create.ui" }
