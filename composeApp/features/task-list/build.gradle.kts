plugins {
  id("crossplatform.library.compose")
  id("crossplatform.screenshot")
}

base { archivesName = "ui-feature-task-list" }

kotlin {
  android { namespace = "com.frame.zero.feature.task.list.ui" }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared.features.taskList)
      implementation(libs.androidx.paging.compose)
      implementation(libs.compose.components.resources)
      implementation(projects.composeApp.shared.designSystem)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
