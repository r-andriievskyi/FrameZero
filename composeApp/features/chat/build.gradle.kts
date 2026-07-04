plugins {
  id("crossplatform.library.compose")
}

base { archivesName = "ui-feature-chat" }

kotlin {
  android { namespace = "com.frame.zero.feature.chat.ui" }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared.features.chat)
      implementation(libs.compose.components.resources)
      implementation(projects.composeApp.shared.designSystem)
      implementation(projects.composeApp.shared.uiText)
      implementation(libs.androidx.paging.compose)
      implementation(libs.kotlinx.datetime)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
