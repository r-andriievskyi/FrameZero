plugins {
  id("crossplatform.library.compose")
  id("crossplatform.screenshot")
}

base { archivesName = "ui-feature-task-create" }

kotlin {
  android { namespace = "com.frame.zero.feature.task.create.ui" }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared.features.taskCreate)
      implementation(libs.compose.components.resources)
      implementation(projects.composeApp.shared.designSystem)
      implementation(projects.composeApp.shared.uiText)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
