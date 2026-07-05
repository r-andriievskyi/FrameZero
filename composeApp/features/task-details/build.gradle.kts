plugins {
  id("crossplatform.library.compose")
  id("crossplatform.screenshot")
}

base { archivesName = "ui-feature-task-details" }

kotlin {
  android { namespace = "com.frame.zero.feature.task.details.ui" }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared.features.taskDetails)
      implementation(libs.compose.components.resources)
      implementation(projects.composeApp.shared.designSystem)
      implementation(projects.composeApp.shared.uiText)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
